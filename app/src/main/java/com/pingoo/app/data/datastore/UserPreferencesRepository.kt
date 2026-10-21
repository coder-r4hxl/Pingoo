package com.pingoo.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pingoo.app.domain.model.Language
import com.pingoo.app.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val DEFAULT_RADIUS = intPreferencesKey("default_radius_meters")
        val DEFAULT_ADVANCE_WARNING = intPreferencesKey("default_advance_warning_meters")
        val SNOOZE_DURATION = intPreferencesKey("snooze_duration_minutes")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val userSettings: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val langCode = preferences[Keys.LANGUAGE_CODE] ?: Language.ENGLISH.code
        UserSettings(
            language = Language.fromCode(langCode),
            isVibrationEnabled = preferences[Keys.VIBRATION_ENABLED] ?: true,
            defaultRadiusMeters = preferences[Keys.DEFAULT_RADIUS] ?: 500,
            defaultAdvanceWarningMeters = preferences[Keys.DEFAULT_ADVANCE_WARNING] ?: 2000,
            snoozeDurationMinutes = preferences[Keys.SNOOZE_DURATION] ?: 5,
            darkTheme = preferences[Keys.DARK_THEME] ?: false
        )
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LANGUAGE_CODE] = languageCode
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setDefaultRadius(radiusMeters: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DEFAULT_RADIUS] = radiusMeters
        }
    }

    suspend fun setSnoozeDuration(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SNOOZE_DURATION] = minutes
        }
    }
}
