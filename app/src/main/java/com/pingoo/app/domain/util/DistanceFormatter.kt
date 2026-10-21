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
}
