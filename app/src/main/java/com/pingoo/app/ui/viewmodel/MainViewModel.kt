package com.pingoo.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pingoo.app.data.datastore.UserPreferencesRepository
import com.pingoo.app.data.local.AppDatabase
import com.pingoo.app.data.repository.DestinationRepositoryImpl
import com.pingoo.app.data.repository.SettingsRepositoryImpl
import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.Language
import com.pingoo.app.domain.model.UserSettings
import com.pingoo.app.domain.usecase.ValidateDestinationUseCase
import com.pingoo.app.domain.usecase.ValidationResult
import com.pingoo.app.ui.i18n.AppStrings
import com.pingoo.app.ui.i18n.Translations
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
        val advanceWarning = userSettings.value.defaultAdvanceWarningMeters

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
}
