package com.pingoo.app.domain.model

enum class JourneyState {
    IDLE,
    TRACKING,
    APPROACHING,
    ARRIVED,
    PAUSED,
    GPS_UNAVAILABLE,
    MISSED
}

data class GpsFix(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampMillis: Long = System.currentTimeMillis(),
    val speedMetersPerSecond: Float? = null,
    val provider: String = "gps"
) {
    val isValidCoordinate: Boolean
        get() = latitude in -90.0..90.0 && longitude in -180.0..180.0
}
