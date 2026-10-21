package com.pingoo.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pingoo.app.MainActivity
import com.pingoo.app.domain.engine.AlarmManagerEngine
import com.pingoo.app.domain.engine.ArrivalStateMachine
import com.pingoo.app.domain.engine.StateMachineEvaluation
import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.GpsFix
import com.pingoo.app.domain.model.JourneyState
import com.pingoo.app.domain.util.DistanceFormatter
import com.pingoo.app.service.alarm.AlarmPlayer
import com.pingoo.app.ui.alarm.AlarmActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationTrackingService : Service(), LocationListener, LocationServiceContract {

    private val binder = LocalBinder()
    private val stateMachine = ArrivalStateMachine()
    private val alarmEngine = AlarmManagerEngine()
    private lateinit var alarmPlayer: AlarmPlayer

    private val _serviceEvaluation = MutableStateFlow<StateMachineEvaluation>(
        StateMachineEvaluation(JourneyState.IDLE, null, "Tracking Service Idle")
    )
    val serviceEvaluation: StateFlow<StateMachineEvaluation> = _serviceEvaluation.asStateFlow()

    private var locationManager: LocationManager? = null
    private var isTrackingActive = false

    inner class LocalBinder : Binder() {
        fun getService(): LocationTrackingService = this@LocationTrackingService
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        alarmPlayer = AlarmPlayer(this)
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initialEval = stateMachine.lastFix?.let {
            stateMachine.processGpsFix(it)
        } ?: StateMachineEvaluation(stateMachine.state, stateMachine.lastDistance, "Tracking Service Active")

        startForeground(NOTIFICATION_ID, buildNotification(initialEval))

        when (intent?.action) {
            ACTION_START_SERVICE -> {
                val destName = intent.getStringExtra(EXTRA_DEST_NAME) ?: "Destination"
                val destLat = intent.getDoubleExtra(EXTRA_DEST_LAT, 0.0)
                val destLng = intent.getDoubleExtra(EXTRA_DEST_LNG, 0.0)
                val destRadius = intent.getIntExtra(EXTRA_DEST_RADIUS, 500)
                val destWarning = intent.getIntExtra(EXTRA_DEST_WARNING, 2000)

                val destination = Destination(
                    name = destName,
                    latitude = destLat,
                    longitude = destLng,
                    arrivalRadiusMeters = destRadius,
                    advanceWarningRadiusMeters = destWarning
                )
                startJourneyTracking(destination)
            }
            ACTION_STOP_SERVICE -> stopMonitoring()
            ACTION_PAUSE_SERVICE -> pauseMonitoring()
            ACTION_RESUME_SERVICE -> resumeMonitoring()
            ACTION_SNOOZE_ALARM -> snoozeAlarm()
            ACTION_DISMISS_ALARM -> dismissAlarm()
        }
        return START_STICKY
    }

    fun startJourneyTracking(destination: Destination) {
        val eval = stateMachine.startTracking(destination)
        _serviceEvaluation.value = eval

        updateNotification(eval)
        isTrackingActive = true
        registerLocationUpdates()
    }

    fun pauseMonitoring() {
        val eval = stateMachine.pauseTracking()
        _serviceEvaluation.value = eval
        alarmPlayer.stopAlarm()
        unregisterLocationUpdates()
        updateNotification(eval)
    }

    fun resumeMonitoring() {
        val eval = stateMachine.resumeTracking()
        _serviceEvaluation.value = eval
        registerLocationUpdates()
        updateNotification(eval)
    }

    fun snoozeAlarm() {
        val destId = stateMachine.destination?.id ?: 0L
        alarmEngine.snoozeArrival(destId)
        alarmPlayer.stopAlarm()
    }

    fun dismissAlarm() {
        val destId = stateMachine.destination?.id ?: 0L
        alarmEngine.acknowledgeArrival(destId)
        alarmPlayer.stopAlarm()
    }

    override fun startMonitoring() {
        // Fallback for contract
    }

    override fun stopMonitoring() {
        val eval = stateMachine.stopTracking()
        _serviceEvaluation.value = eval
        isTrackingActive = false
        alarmPlayer.stopAlarm()
        unregisterLocationUpdates()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun isMonitoring(): Boolean = isTrackingActive

    private fun registerLocationUpdates() {
        val hasFine = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            val eval = stateMachine.markGpsUnavailable("Location permissions not granted")
            _serviceEvaluation.value = eval
            updateNotification(eval)
            return
        }

        try {
            locationManager?.let { lm ->
                val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (!gpsEnabled && !networkEnabled) {
                    val eval = stateMachine.markGpsUnavailable("GPS and Network location providers disabled")
                    _serviceEvaluation.value = eval
                    updateNotification(eval)
                    return
                }

                if (gpsEnabled) {
                    lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BETWEEN_UPDATES_MS,
                        MIN_DISTANCE_CHANGE_METERS,
                        this
                    )
                } else if (networkEnabled) {
                    lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BETWEEN_UPDATES_MS,
                        MIN_DISTANCE_CHANGE_METERS,
                        this
                    )
                }
            }
        } catch (e: SecurityException) {
            val eval = stateMachine.markGpsUnavailable("Location permission security exception")
            _serviceEvaluation.value = eval
            updateNotification(eval)
        }
    }

    private fun unregisterLocationUpdates() {
        try {
            locationManager?.removeUpdates(this)
        } catch (e: Exception) {
            // Ignore on cleanup
        }
    }

    override fun onLocationChanged(location: Location) {
        val fix = GpsFix(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = location.accuracy,
            timestampMillis = location.time,
            speedMetersPerSecond = if (location.hasSpeed()) location.speed else null,
            provider = location.provider ?: "unknown"
        )
        val eval = stateMachine.processGpsFix(fix)
        _serviceEvaluation.value = eval

        if (eval.state == JourneyState.ARRIVED) {
            val destId = stateMachine.destination?.id ?: 0L
            if (alarmEngine.isArrivalAlertAllowed(destId)) {
                alarmPlayer.startAlarm(vibrate = stateMachine.destination?.vibrate ?: true)
                launchAlarmActivity()
            }
        }

        updateNotification(eval)
    }

    private fun launchAlarmActivity() {
        val destName = stateMachine.destination?.name ?: "Destination"
        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmActivity.EXTRA_DEST_NAME, destName)
        }
        startActivity(alarmIntent)
    }

    override fun onProviderEnabled(provider: String) {
        if (isTrackingActive) registerLocationUpdates()
    }

    override fun onProviderDisabled(provider: String) {
        val eval = stateMachine.markGpsUnavailable("Location provider ($provider) disabled")
        _serviceEvaluation.value = eval
        updateNotification(eval)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pingoo Journey Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live GPS journey progress and destination arrival alerts"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(eval: StateMachineEvaluation): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, LocationTrackingService::class.java).apply { action = ACTION_STOP_SERVICE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val destName = stateMachine.destination?.name ?: "Destination"
        val distText = DistanceFormatter.formatDistance(eval.distanceMeters)

        val contentTitle = when (eval.state) {
            JourneyState.ARRIVED -> "Arrived at $destName!"
            JourneyState.APPROACHING -> "Approaching $destName"
            JourneyState.TRACKING -> "Tracking journey to $destName"
            JourneyState.PAUSED -> "Journey to $destName paused"
            JourneyState.GPS_UNAVAILABLE -> "GPS Signal Issue"
            else -> "Pingoo Journey Tracker"
        }

        val contentText = "Distance: $distText • Status: ${eval.state}"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(eval.state != JourneyState.IDLE && eval.state != JourneyState.ARRIVED)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(eval: StateMachineEvaluation) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(eval))
    }

    override fun onDestroy() {
        alarmPlayer.stopAlarm()
        unregisterLocationUpdates()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "pingoo_journey_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START_SERVICE = "com.pingoo.app.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.pingoo.app.STOP_SERVICE"
        const val ACTION_PAUSE_SERVICE = "com.pingoo.app.PAUSE_SERVICE"
        const val ACTION_RESUME_SERVICE = "com.pingoo.app.RESUME_SERVICE"
        const val ACTION_SNOOZE_ALARM = "com.pingoo.app.SNOOZE_ALARM"
        const val ACTION_DISMISS_ALARM = "com.pingoo.app.DISMISS_ALARM"

        const val EXTRA_DEST_NAME = "extra_dest_name"
        const val EXTRA_DEST_LAT = "extra_dest_lat"
        const val EXTRA_DEST_LNG = "extra_dest_lng"
        const val EXTRA_DEST_RADIUS = "extra_dest_radius"
        const val EXTRA_DEST_WARNING = "extra_dest_warning"

        private const val MIN_TIME_BETWEEN_UPDATES_MS = 2000L // 2 seconds
        private const val MIN_DISTANCE_CHANGE_METERS = 5.0f // 5 meters
    }
}
