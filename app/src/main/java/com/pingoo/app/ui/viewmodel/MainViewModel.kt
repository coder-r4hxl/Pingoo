package com.pingoo.app.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pingoo.app.data.datastore.UserPreferencesRepository
import com.pingoo.app.data.local.AppDatabase
import com.pingoo.app.data.repository.DestinationRepositoryImpl
import com.pingoo.app.data.repository.SettingsRepositoryImpl
import com.pingoo.app.domain.engine.StateMachineEvaluation
import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.JourneyState
import com.pingoo.app.domain.model.Language
import com.pingoo.app.domain.model.UserSettings
import com.pingoo.app.domain.usecase.ValidateDestinationUseCase
import com.pingoo.app.domain.usecase.ValidationResult
import com.pingoo.app.service.LocationTrackingService
import com.pingoo.app.ui.i18n.AppStrings
import com.pingoo.app.ui.i18n.Translations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val destinationDao = db.destinationDao()
    private val destinationRepository = DestinationRepositoryImpl(destinationDao)

    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val settingsRepository = SettingsRepositoryImpl(userPreferencesRepository)

    private val validateDestinationUseCase = ValidateDestinationUseCase()

    private var trackingService: LocationTrackingService? = null
    private var isServiceBound = false

    private val _selectedDestination = MutableStateFlow<Destination?>(null)
    val selectedDestination: StateFlow<Destination?> = _selectedDestination.asStateFlow()

    private val _journeyEvaluation = MutableStateFlow(
        StateMachineEvaluation(JourneyState.IDLE, null, "Idle")
    )
    val journeyEvaluation: StateFlow<StateMachineEvaluation> = _journeyEvaluation.asStateFlow()

    val userSettings: StateFlow<UserSettings> = settingsRepository.getUserSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    val strings: StateFlow<AppStrings> = userSettings.map { settings ->
        Translations.get(settings.language)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Translations.get(Language.ENGLISH)
    )

    val destinations: StateFlow<List<Destination>> = destinationRepository.getAllDestinations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeDestinations: StateFlow<List<Destination>> = destinationRepository.getActiveDestinations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as? LocationTrackingService.LocalBinder
            trackingService = localBinder?.getService()
            isServiceBound = true

            trackingService?.let { service ->
                viewModelScope.launch {
                    service.serviceEvaluation.collect { eval ->
                        _journeyEvaluation.value = eval
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
            isServiceBound = false
        }
    }

    init {
        bindTrackingService()
    }

    private fun bindTrackingService() {
        val intent = Intent(getApplication(), LocationTrackingService::class.java)
        getApplication<Application>().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun selectDestination(destination: Destination?) {
        _selectedDestination.value = destination
    }

    fun startJourney(destination: Destination) {
        _selectedDestination.value = destination
        val context = getApplication<Application>()
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_SERVICE
            putExtra(LocationTrackingService.EXTRA_DEST_NAME, destination.name)
            putExtra(LocationTrackingService.EXTRA_DEST_LAT, destination.latitude)
            putExtra(LocationTrackingService.EXTRA_DEST_LNG, destination.longitude)
            putExtra(LocationTrackingService.EXTRA_DEST_RADIUS, destination.arrivalRadiusMeters)
            putExtra(LocationTrackingService.EXTRA_DEST_WARNING, destination.advanceWarningRadiusMeters)
        }
        context.startForegroundService(intent)

        if (!isServiceBound) {
            bindTrackingService()
        }
    }

    fun pauseJourney() {
        trackingService?.pauseMonitoring()
    }

    fun resumeJourney() {
        trackingService?.resumeMonitoring()
    }

    fun snoozeAlarm() {
        trackingService?.snoozeAlarm()
    }

    fun dismissAlarm() {
        trackingService?.dismissAlarm()
    }

    fun stopJourney() {
        trackingService?.stopMonitoring()
    }

    fun changeLanguage(language: Language) {
        viewModelScope.launch {
            settingsRepository.updateLanguage(language.code)
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateVibrationEnabled(enabled)
        }
    }

    fun toggleDestinationActive(destination: Destination) {
        viewModelScope.launch {
            destinationRepository.toggleDestinationActive(destination.id, !destination.isActive)
        }
    }

    fun addDestination(
        name: String,
        latStr: String,
        lngStr: String,
        radiusStr: String,
        onResult: (ValidationResult) -> Unit
    ) {
        val lat = latStr.toDoubleOrNull()
        val lng = lngStr.toDoubleOrNull()
        val radius = radiusStr.toIntOrNull() ?: userSettings.value.defaultRadiusMeters
        val defaultWarning = userSettings.value.defaultAdvanceWarningMeters
        val advanceWarning = maxOf(defaultWarning, radius + 500)

        val validation = validateDestinationUseCase(
            name = name,
            latitude = lat,
            longitude = lng,
            arrivalRadiusMeters = radius,
            advanceWarningMeters = advanceWarning
        )

        if (validation is ValidationResult.Success) {
            viewModelScope.launch {
                destinationRepository.insertDestination(
                    Destination(
                        name = name.trim(),
                        latitude = lat!!,
                        longitude = lng!!,
                        arrivalRadiusMeters = radius,
                        advanceWarningRadiusMeters = advanceWarning
                    )
                )
            }
        }
        onResult(validation)
    }

    fun deleteDestination(destination: Destination) {
        viewModelScope.launch {
            destinationRepository.deleteDestination(destination)
        }
    }

    override fun onCleared() {
        if (isServiceBound) {
            try {
                getApplication<Application>().unbindService(serviceConnection)
            } catch (e: Exception) {
                // Ignore unbind errors
            }
            isServiceBound = false
        }
        super.onCleared()
    }
}
