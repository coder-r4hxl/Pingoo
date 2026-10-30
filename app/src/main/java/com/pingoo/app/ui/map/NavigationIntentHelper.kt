package com.pingoo.app.ui.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import java.util.Locale

object NavigationIntentHelper {

    fun isValidCoordinates(latitude: Double, longitude: Double): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    fun buildGeoUriString(latitude: Double, longitude: Double, label: String? = null): String {
        require(isValidCoordinates(latitude, longitude)) {
            "Invalid coordinates: lat=$latitude, lng=$longitude"
        }
        val latStr = String.format(Locale.US, "%.6f", latitude)
        val lngStr = String.format(Locale.US, "%.6f", longitude)
        val rawLabel = label ?: "Destination"
        val encodedLabel = try {
            URLEncoder.encode(rawLabel, "UTF-8").replace("+", "%20")
        } catch (e: Exception) {
            rawLabel
        }
        return "geo:$latStr,$lngStr?q=$latStr,$lngStr($encodedLabel)"
    }

    fun launchNavigationIntent(
        context: Context,
        latitude: Double,
        longitude: Double,
        label: String? = null
    ): Boolean {
        if (!isValidCoordinates(latitude, longitude)) return false
        val uriString = buildGeoUriString(latitude, longitude, label)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
