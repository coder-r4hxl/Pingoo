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
import com.pingoo.app.data.datastore.UserPreferencesRepository
import com.pingoo.app.domain.engine.MultiStopEngine
import com.pingoo.app.domain.engine.MultiStopEvaluation
import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.GpsFix
import com.pingoo.app.domain.model.JourneyState
import com.pingoo.app.domain.model.JourneyStop
import com.pingoo.app.domain.model.Language
import com.pingoo.app.domain.model.MultiStopJourney
import com.pingoo.app.domain.util.DistanceFormatter
import com.pingoo.app.service.alarm.AlarmPlayer
import com.pingoo.app.ui.alarm.AlarmActivity
import com.pingoo.app.ui.i18n.Translations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class LocationTrackingService : Service(), LocationListener, LocationServiceContract {

    private val binder = LocalBinder()
    val multiStopEngine = MultiStopEngine()
    private lateinit var alarmPlayer: AlarmPlayer
    private lateinit var userPreferencesRepository: UserPreferencesRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var currentLanguage: Language = Language.ENGLISH

    private val _multiStopEvaluation = MutableStateFlow<MultiStopEvaluation>(
        MultiStopEvaluation(JourneyState.IDLE, null, 0, 0, null, "Tracking Service Idle")
    )
    val multiStopEvaluation: StateFlow<MultiStopEvaluation> = _multiStopEvaluation.asStateFlow()

    private var locationManager: LocationManager? = null
    private var isTrackingActive = false
    private var currentPresentedArrivalKey: String? = null

    inner class LocalBinder : Binder() {
        fun getService(): LocationTrackingService = this@LocationTrackingService
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        alarmPlayer = AlarmPlayer(this)
        userPreferencesRepository = UserPreferencesRepository(this)
        createNotificationChannels()

        serviceScope.launch {
            userPreferencesRepository.userSettings.collect { settings ->
                currentLanguage = settings.language
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val initialEval = _multiStopEvaluation.value
        startForegroundCompat(NOTIFICATION_ID, buildTrackingNotification(initialEval))

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
        currentPresentedArrivalKey = null

        updateNotification(eval)
        isTrackingActive = true
        registerLocationUpdates()
    }

    fun pauseMonitoring() {
        val eval = multiStopEngine.pauseJourney()
        _multiStopEvaluation.value = eval
        alarmPlayer.stopAlarm()
        cancelArrivalNotification()
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
        val stop = multiStopEngine.journey?.currentStop
        val key = if (stop != null) {
            multiStopEngine.alarmEngine.makeOccurrenceKey(
                stop.destination.id,
                stop.stopOrder,
                multiStopEngine.journey?.id ?: 0L
            )
        } else "0"
        multiStopEngine.alarmEngine.snoozeArrival(key)
        alarmPlayer.stopAlarm()
        cancelArrivalNotification()
    }

    fun dismissAlarmAndAdvance() {
        alarmPlayer.stopAlarm()
        cancelArrivalNotification()
        currentPresentedArrivalKey = null
        val eval = multiStopEngine.acknowledgeActiveStopAndAdvance()
        _multiStopEvaluation.value = eval
        updateNotification(eval)

        if (eval.isJourneyCompleted) {
            stopMonitoring()
        }
    }

    override fun startMonitoring() {}

    override fun stopMonitoring() {
        val eval = multiStopEngine.stopJourney()
        _multiStopEvaluation.value = eval
        isTrackingActive = false
        currentPresentedArrivalKey = null
        alarmPlayer.stopAlarm()
        cancelArrivalNotification()
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
            val stop = eval.activeStop
            val destId = stop?.destination?.id ?: 0L
            val occurrenceKey = multiStopEngine.alarmEngine.makeOccurrenceKey(
                destId,
                stop?.stopOrder ?: 0,
                multiStopEngine.journey?.id ?: 0L
            )

            if (multiStopEngine.alarmEngine.isArrivalAlertAllowed(occurrenceKey)) {
                // Trigger once per unique arrival event to prevent loop
                if (currentPresentedArrivalKey != occurrenceKey) {
                    currentPresentedArrivalKey = occurrenceKey
                    alarmPlayer.startAlarm(vibrate = stop?.destination?.vibrate ?: true)
                    presentArrivalAlert(stop?.destination?.name ?: "Destination")
                }
            }
        }

        updateNotification(eval)
    }

    private fun presentArrivalAlert(destinationName: String) {
        val strings = Translations.get(currentLanguage)

        // Launch full-screen lock overlay if possible, or fallback gracefully
        val alarmIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AlarmActivity.EXTRA_DEST_NAME, destinationName)
            putExtra(AlarmActivity.EXTRA_LANGUAGE, currentLanguage.code)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            ARRIVAL_PENDING_INTENT_ID,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, LocationTrackingService::class.java).apply { action = ACTION_SNOOZE_ALARM },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = PendingIntent.getService(
            this,
            5,
            Intent(this, LocationTrackingService::class.java).apply { action = ACTION_DISMISS_ALARM },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val arrivalNotification = NotificationCompat.Builder(this, ARRIVAL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("${strings.alarmArrivalHeader}: $destinationName")
            .setContentText(strings.alarmArrivedMessage)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_popup_sync, strings.alarmSnoozeButton, snoozeIntent)
            .addAction(android.R.drawable.ic_menu_compass, strings.alarmDismissButton, dismissIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ARRIVAL_NOTIFICATION_ID, arrivalNotification)

        // Also attempt direct activity start for foreground scenarios
        try {
            startActivity(alarmIntent)
        } catch (e: Exception) {
            // Background start can be blocked by Android 10+; high-importance notification handles it
        }
    }

    private fun cancelArrivalNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(ARRIVAL_NOTIFICATION_ID)
    }

    override fun onProviderEnabled(provider: String) {
        if (isTrackingActive) registerLocationUpdates()
    }

    override fun onProviderDisabled(provider: String) {
        val eval = multiStopEngine.stateMachine.markGpsUnavailable("Location provider ($provider) disabled")
        updateFromStateMachine(eval.state, eval.statusMessage)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val trackingChannel = NotificationChannel(
                TRACKING_CHANNEL_ID,
                "Pingoo Journey Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live GPS journey progress"
            }

            val arrivalChannel = NotificationChannel(
                ARRIVAL_CHANNEL_ID,
                "Pingoo Arrival Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Heads-up alerts and full-screen arrival alarms"
                enableVibration(true)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(trackingChannel)
            manager.createNotificationChannel(arrivalChannel)
        }
    }

    private fun buildTrackingNotification(eval: MultiStopEvaluation): Notification {
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

        val pauseIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, LocationTrackingService::class.java).apply { action = ACTION_PAUSE_SERVICE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resumeIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, LocationTrackingService::class.java).apply { action = ACTION_RESUME_SERVICE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val strings = Translations.get(currentLanguage)
        val stopName = eval.activeStop?.destination?.name ?: "Destination"
        val distText = DistanceFormatter.formatDistance(eval.distanceToActiveStopMeters, currentLanguage)
        val stopProgressText = if (eval.totalStops > 0) "Stop ${eval.activeStopIndex + 1}/${eval.totalStops}: $stopName" else stopName

        val contentTitle = when (eval.journeyState) {
            JourneyState.ARRIVED -> "${strings.alarmArrivalHeader} $stopName!"
            JourneyState.APPROACHING -> "Approaching $stopName"
            JourneyState.TRACKING -> "Tracking to $stopName"
            JourneyState.PAUSED -> strings.pauseMonitoring
            JourneyState.GPS_UNAVAILABLE -> strings.liveGpsStatus
            else -> strings.appName
        }

        val contentText = "$stopProgressText • Rem: $distText"

        val builder = NotificationCompat.Builder(this, TRACKING_CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(eval.journeyState != JourneyState.IDLE && eval.journeyState != JourneyState.ARRIVED)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (eval.journeyState == JourneyState.TRACKING || eval.journeyState == JourneyState.APPROACHING) {
            builder.addAction(android.R.drawable.ic_media_pause, strings.pauseMonitoring, pauseIntent)
        } else if (eval.journeyState == JourneyState.PAUSED) {
            builder.addAction(android.R.drawable.ic_media_play, strings.resumeMonitoring, resumeIntent)
        }

        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, strings.stopMonitoring, stopIntent)

        return builder.build()
    }

    private fun updateNotification(eval: MultiStopEvaluation) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildTrackingNotification(eval))
    }

    override fun onDestroy() {
        alarmPlayer.stopAlarm()
        cancelArrivalNotification()
        unregisterLocationUpdates()
        super.onDestroy()
    }

    companion object {
        const val TRACKING_CHANNEL_ID = "pingoo_journey_channel"
        const val ARRIVAL_CHANNEL_ID = "pingoo_arrival_channel"

        const val NOTIFICATION_ID = 2001
        const val ARRIVAL_NOTIFICATION_ID = 2002
        const val ARRIVAL_PENDING_INTENT_ID = 2003

        const val ACTION_START_SERVICE = "com.pingoo.app.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.pingoo.app.STOP_SERVICE"
        const val ACTION_PAUSE_SERVICE = "com.pingoo.app.PAUSE_SERVICE"
        const val ACTION_RESUME_SERVICE = "com.pingoo.app.RESUME_SERVICE"
        const val ACTION_SNOOZE_ALARM = "com.pingoo.app.SNOOZE_ALARM"
        const val ACTION_DISMISS_ALARM = "com.pingoo.app.DISMISS_ALARM"

        private const val MIN_TIME_BETWEEN_UPDATES_MS = 2000L // 2 seconds
        private const val MIN_DISTANCE_CHANGE_METERS = 5.0f // 5 meters
    }
}
