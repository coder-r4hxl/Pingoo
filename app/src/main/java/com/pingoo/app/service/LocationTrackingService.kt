package com.pingoo.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pingoo.app.MainActivity
import com.pingoo.app.domain.engine.MultiStopEngine
import com.pingoo.app.domain.engine.MultiStopEvaluation
import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.GpsFix
import com.pingoo.app.domain.model.JourneyState
import com.pingoo.app.domain.model.JourneyStop
import com.pingoo.app.domain.model.MultiStopJourney
import com.pingoo.app.domain.util.DistanceFormatter
import com.pingoo.app.service.alarm.AlarmPlayer
import com.pingoo.app.ui.alarm.AlarmActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationTrackingService : Service(), LocationListener, LocationServiceContract {

    private val binder = LocalBinder()
    private val multiStopEngine = MultiStopEngine()
    private lateinit var alarmPlayer: AlarmPlayer

    private val _multiStopEvaluation = MutableStateFlow<MultiStopEvaluation>(
        MultiStopEvaluation(JourneyState.IDLE, null, 0, 0, null, "Tracking Service Idle")
    )
    val multiStopEvaluation: StateFlow<MultiStopEvaluation> = _multiStopEvaluation.asStateFlow()

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
        val initialEval = _multiStopEvaluation.value
        startForegroundCompat(NOTIFICATION_ID, buildNotification(initialEval))

        when (intent?.action) {
            ACTION_STOP_SERVICE -> stopMonitoring()
            ACTION_PAUSE_SERVICE -> pauseMonitoring()
            ACTION_RESUME_SERVICE -> resumeMonitoring()
            ACTION_SNOOZE_ALARM -> snoozeAlarm()
            ACTION_DISMISS_ALARM -> dismissAlarmAndAdvance()
        }
        return START_STICKY
    }

    private fun startForegroundCompat(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(id, notification)
        }
    }

    fun startMultiStopJourney(journey: MultiStopJourney) {
        val eval = multiStopEngine.startJourney(journey)
        _multiStopEvaluation.value = eval

        updateNotification(eval)
        isTrackingActive = true
        registerLocationUpdates()
    }

    fun pauseMonitoring() {
        val eval = multiStopEngine.pauseJourney()
        _multiStopEvaluation.value = eval
        alarmPlayer.stopAlarm()
        unregisterLocationUpdates()
        updateNotification(eval)
    }

    fun resumeMonitoring() {
        val eval = multiStopEngine.resumeJourney()
        _multiStopEvaluation.value = eval
        registerLocationUpdates()
        updateNotification(eval)
    }

    fun snoozeAlarm() {
        val destId = multiStopEngine.journey?.currentStop?.destination?.id ?: 0L
        multiStopEngine.alarmEngine.snoozeArrival(destId)
        alarmPlayer.stopAlarm()
    }

    fun dismissAlarmAndAdvance() {
        alarmPlayer.stopAlarm()
        val eval = multiStopEngine.acknowledgeActiveStopAndAdvance()
        _multiStopEvaluation.value = eval
        updateNotification(eval)

        if (eval.isJourneyCompleted) {
            stopMonitoring()
        }
    }

    override fun startMonitoring() {
        // Fallback for contract
    }

    override fun stopMonitoring() {
        val eval = multiStopEngine.stopJourney()
        _multiStopEvaluation.value = eval
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
            val eval = multiStopEngine.stateMachine.markGpsUnavailable("Location permissions not granted")
            updateFromStateMachine(eval.state, eval.statusMessage)
            return
        }

        try {
            locationManager?.let { lm ->
                val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (!gpsEnabled && !networkEnabled) {
                    val eval = multiStopEngine.stateMachine.markGpsUnavailable("GPS and Network location providers disabled")
                    updateFromStateMachine(eval.state, eval.statusMessage)
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
            val eval = multiStopEngine.stateMachine.markGpsUnavailable("Location permission security exception")
            updateFromStateMachine(eval.state, eval.statusMessage)
        }
    }

    private fun updateFromStateMachine(state: JourneyState, statusMessage: String) {
        val current = _multiStopEvaluation.value
        val updated = current.copy(journeyState = state, statusMessage = statusMessage)
        _multiStopEvaluation.value = updated
        updateNotification(updated)
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
        val eval = multiStopEngine.processGpsFix(fix)
        _multiStopEvaluation.value = eval

        if (eval.journeyState == JourneyState.ARRIVED) {
            val destId = eval.activeStop?.destination?.id ?: 0L
            if (multiStopEngine.alarmEngine.isArrivalAlertAllowed(destId)) {
                alarmPlayer.startAlarm(vibrate = eval.activeStop?.destination?.vibrate ?: true)
                launchAlarmActivity(eval.activeStop?.destination?.name ?: "Destination")
            }
        }

        updateNotification(eval)
    }

    private fun launchAlarmActivity(destinationName: String) {
        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmActivity.EXTRA_DEST_NAME, destinationName)
        }
        startActivity(alarmIntent)
    }

    override fun onProviderEnabled(provider: String) {
        if (isTrackingActive) registerLocationUpdates()
    }

    override fun onProviderDisabled(provider: String) {
        val eval = multiStopEngine.stateMachine.markGpsUnavailable("Location provider ($provider) disabled")
        updateFromStateMachine(eval.state, eval.statusMessage)
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

    private fun buildNotification(eval: MultiStopEvaluation): Notification {
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

        val stopName = eval.activeStop?.destination?.name ?: "Destination"
        val distText = DistanceFormatter.formatDistance(eval.distanceToActiveStopMeters)
        val stopProgressText = if (eval.totalStops > 0) "Stop ${eval.activeStopIndex + 1}/${eval.totalStops}: $stopName" else stopName

        val contentTitle = when (eval.journeyState) {
            JourneyState.ARRIVED -> "Arrived at $stopName!"
            JourneyState.APPROACHING -> "Approaching $stopName"
            JourneyState.TRACKING -> "Tracking to $stopName"
            JourneyState.PAUSED -> "Journey Paused"
            JourneyState.GPS_UNAVAILABLE -> "GPS Signal Issue"
            else -> "Pingoo Journey Tracker"
        }

        val contentText = "$stopProgressText • Rem: $distText"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(eval.journeyState != JourneyState.IDLE && eval.journeyState != JourneyState.ARRIVED)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(eval: MultiStopEvaluation) {
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
