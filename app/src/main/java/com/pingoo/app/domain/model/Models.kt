package com.pingoo.app.domain.model

enum class Language(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी");

    companion object {
        fun fromCode(code: String): Language {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

data class Destination(
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val arrivalRadiusMeters: Int = 500,
    val advanceWarningRadiusMeters: Int = 2000,
    val isActive: Boolean = true,
    val alarmSound: String = "default",
    val vibrate: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class UserSettings(
    val language: Language = Language.ENGLISH,
    val isVibrationEnabled: Boolean = true,
    val defaultRadiusMeters: Int = 500,
    val defaultAdvanceWarningMeters: Int = 2000,
    val darkTheme: Boolean = false
)

data class JourneyStatus(
    val isMonitoring: Boolean = false,
    val activeDestinationCount: Int = 0,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val currentAccuracyMeters: Float? = null,
    val nearestDestinationName: String? = null,
    val distanceToNearestMeters: Double? = null
)
