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
import com.pingoo.app.data.local.entity.JourneyEntity
import com.pingoo.app.data.local.entity.JourneyHistoryEntity
import com.pingoo.app.data.local.entity.JourneyStopEntity
import com.pingoo.app.data.repository.DestinationRepositoryImpl
import com.pingoo.app.data.repository.SettingsRepositoryImpl
import com.pingoo.app.domain.engine.MultiStopEvaluation
import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.JourneyHistoryItem
import com.pingoo.app.domain.model.JourneyState
import com.pingoo.app.domain.model.JourneyStop
import com.pingoo.app.domain.model.Language
import com.pingoo.app.domain.model.MultiStopJourney
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
    private val journeyDao = db.journeyDao()
    private val journeyHistoryDao = db.journeyHistoryDao()

    private val destinationRepository = DestinationRepositoryImpl(destinationDao)
    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val settingsRepository = SettingsRepositoryImpl(userPreferencesRepository)

    private val validateDestinationUseCase = ValidateDestinationUseCase()

    private var trackingService: LocationTrackingService? = null
    private var isServiceBound = false

    private var currentJourneyStartTimeMillis: Long = 0L

    private val _selectedDestination = MutableStateFlow<Destination?>(null)
    val selectedDestination: StateFlow<Destination?> = _selectedDestination.asStateFlow()

    private val _multiStopEvaluation = MutableStateFlow(
        MultiStopEvaluation(JourneyState.IDLE, null, 0, 0, null, "Idle")
    )
    val multiStopEvaluation: StateFlow<MultiStopEvaluation> = _multiStopEvaluation.asStateFlow()

    // Multi-stop journey creation state
    private val _stagedStops = MutableStateFlow<List<Destination>>(emptyList())
    val stagedStops: StateFlow<List<Destination>> = _stagedStops.asStateFlow()

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

    val journeyHistory: StateFlow<List<JourneyHistoryItem>> = journeyHistoryDao.getAllHistory()
        .map { list ->
            list.map { entity ->
                JourneyHistoryItem(
                    id = entity.id,
                    title = entity.title,
                    totalStops = entity.totalStops,
                    reachedStops = entity.reachedStops,
                    isCompleted = entity.isCompleted,
                    durationSeconds = entity.durationSeconds,
                    timestampMillis = entity.timestampMillis
                )
            }
        }
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
                    service.multiStopEvaluation.collect { eval ->
                        _multiStopEvaluation.value = eval
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

    fun addStagedStop(destination: Destination) {
        _stagedStops.value = _stagedStops.value + destination
    }

    fun removeStagedStop(index: Int) {
        if (index in _stagedStops.value.indices) {
            val current = _stagedStops.value.toMutableList()
            current.removeAt(index)
            _stagedStops.value = current
        }
    }

    fun moveStagedStopUp(index: Int) {
        if (index > 0 && index < _stagedStops.value.size) {
            val list = _stagedStops.value.toMutableList()
            val item = list.removeAt(index)
            list.add(index - 1, item)
            _stagedStops.value = list
        }
    }

    fun moveStagedStopDown(index: Int) {
        if (index >= 0 && index < _stagedStops.value.size - 1) {
            val list = _stagedStops.value.toMutableList()
            val item = list.removeAt(index)
            list.add(index + 1, item)
            _stagedStops.value = list
        }
    }

    fun clearStagedStops() {
        _stagedStops.value = emptyList()
    }

    fun startSingleDestinationJourney(destination: Destination) {
        val singleStopJourney = MultiStopJourney(
            title = "Journey to ${destination.name}",
            stops = listOf(
                JourneyStop(
                    destination = destination,
                    stopOrder = 0
                )
            )
        )
        startMultiStopJourney(singleStopJourney)
    }

    fun startStagedMultiStopJourney(title: String) {
        if (_stagedStops.value.isEmpty()) return

        val stops = _stagedStops.value.mapIndexed { index, dest ->
            JourneyStop(
                destination = dest,
                stopOrder = index
            )
        }

        val journey = MultiStopJourney(
            title = if (title.isBlank()) "Multi-Stop Journey (${stops.size} stops)" else title,
            stops = stops
        )

        viewModelScope.launch {
            val journeyId = journeyDao.insertJourney(
                JourneyEntity(
                    title = journey.title,
                    activeStopIndex = 0
                )
            )
            val stopEntities = stops.mapIndexed { idx, stop ->
                JourneyStopEntity(
                    journeyId = journeyId,
                    destinationId = stop.destination.id,
                    stopOrder = idx
                )
            }
            journeyDao.insertJourneyStops(stopEntities)
        }

        startMultiStopJourney(journey)
    }

    fun startMultiStopJourney(journey: MultiStopJourney) {
        currentJourneyStartTimeMillis = System.currentTimeMillis()
        val context = getApplication<Application>()
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_SERVICE
        }
        context.startForegroundService(intent)

        if (isServiceBound && trackingService != null) {
            trackingService?.startMultiStopJourney(journey)
        } else {
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

    fun dismissAlarmAndAdvance() {
        trackingService?.dismissAlarmAndAdvance()
    }

    fun stopJourney() {
        val eval = _multiStopEvaluation.value
        val durationSecs = if (currentJourneyStartTimeMillis > 0) {
            (System.currentTimeMillis() - currentJourneyStartTimeMillis) / 1000L
        } else 0L

        if (eval.totalStops > 0) {
            viewModelScope.launch {
                journeyHistoryDao.insertHistory(
                    JourneyHistoryEntity(
                        title = eval.activeStop?.destination?.name ?: "Journey",
                        totalStops = eval.totalStops,
                        reachedStops = eval.activeStopIndex + if (eval.isJourneyCompleted) 1 else 0,
                        isCompleted = eval.isJourneyCompleted,
                        durationSeconds = durationSecs
                    )
                )
            }
        }

        trackingService?.stopMonitoring()
        currentJourneyStartTimeMillis = 0L
    }

    fun deleteHistoryItem(item: JourneyHistoryItem) {
        viewModelScope.launch {
            journeyHistoryDao.deleteHistory(
                JourneyHistoryEntity(
                    id = item.id,
                    title = item.title,
                    totalStops = item.totalStops,
                    reachedStops = item.reachedStops,
                    isCompleted = item.isCompleted,
                    durationSeconds = item.durationSeconds,
                    timestampMillis = item.timestampMillis
                )
            )
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            journeyHistoryDao.deleteAllHistory()
        }
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

    fun setSnoozeDuration(minutes: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setSnoozeDuration(minutes)
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
