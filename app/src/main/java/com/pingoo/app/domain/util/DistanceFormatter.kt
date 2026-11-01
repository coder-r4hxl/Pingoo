package com.pingoo.app.domain.util

import com.pingoo.app.domain.model.Language
import java.util.Locale
import kotlin.math.roundToInt

object DistanceFormatter {

    fun formatDistance(distanceMeters: Double?, language: Language = Language.ENGLISH): String {
        if (distanceMeters == null || distanceMeters.isNaN() || distanceMeters < 0) {
            return "--"
        }

        return if (distanceMeters >= 1000.0) {
            val km = distanceMeters / 1000.0
            val formattedKm = String.format(Locale.US, "%.1f", km)
            when (language) {
                Language.HINDI -> "$formattedKm कि.मी."
                Language.ENGLISH -> "$formattedKm km"
            }
        } else {
            val meters = distanceMeters.roundToInt()
            when (language) {
                Language.HINDI -> "$meters मी."
                Language.ENGLISH -> "$meters m"
            }
        }
    }

    fun formatAccuracy(accuracyMeters: Float?, language: Language = Language.ENGLISH): String {
        if (accuracyMeters == null || accuracyMeters.isNaN() || accuracyMeters < 0) {
            return "--"
        }
        val meters = accuracyMeters.roundToInt()
        return when (language) {
            Language.HINDI -> "±$meters मी."
            Language.ENGLISH -> "±$meters m"
        }
    }

    /**
     * Calculates linear estimated time of arrival strictly based on measured GPS speed.
     * If speed is unavailable, <= 1.0 m/s (~3.6 km/h, near stationary), or distance is invalid,
     * returns null to guarantee route-based ETA is NEVER fabricated.
     */
    fun calculateSpeedBasedEtaSeconds(
        distanceMeters: Double?,
        speedMetersPerSecond: Float?
    ): Long? {
        if (distanceMeters == null || distanceMeters <= 0 || speedMetersPerSecond == null) {
            return null
        }
        // Require meaningful movement speed (> 1.0 m/s) to avoid inaccurate ETAs
        if (speedMetersPerSecond <= 1.0f) {
            return null
        }
        return (distanceMeters / speedMetersPerSecond).toLong()
    }

    fun formatEta(
        distanceMeters: Double?,
        speedMetersPerSecond: Float?,
        language: Language = Language.ENGLISH
    ): String {
        val seconds = calculateSpeedBasedEtaSeconds(distanceMeters, speedMetersPerSecond)
            ?: return when (language) {
                Language.HINDI -> "अनुपलब्ध (गति आवश्यक)"
                Language.ENGLISH -> "Unavailable (Movement required)"
            }

        val minutes = (seconds + 30) / 60
        return when {
            minutes < 1 -> when (language) {
                Language.HINDI -> "< 1 मिनट"
                Language.ENGLISH -> "< 1 min"
            }
            minutes >= 60 -> {
                val hrs = minutes / 60
                val remMins = minutes % 60
                when (language) {
                    Language.HINDI -> "$hrs घंटा $remMins मिनट"
                    Language.ENGLISH -> "${hrs}h ${remMins}m"
                }
            }
            else -> when (language) {
                Language.HINDI -> "$minutes मिनट"
                Language.ENGLISH -> "$minutes min"
            }
        }
    }
}
