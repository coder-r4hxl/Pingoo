package com.pingoo.app.ui.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.pingoo.app.domain.model.Language
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

data class ReverseGeocodeResult(
    val addressText: String,
    val isVerified: Boolean
)

data class PlaceSearchResult(
    val addressName: String,
    val latitude: Double,
    val longitude: Double
)

object GeocodingHelper {

    suspend fun reverseGeocode(
        context: Context,
        latitude: Double,
        longitude: Double,
        language: Language = Language.ENGLISH
    ): ReverseGeocodeResult = withContext(Dispatchers.IO) {
        val locale = when (language) {
            Language.HINDI -> Locale("hi", "IN")
            Language.ENGLISH -> Locale.ENGLISH
        }

        try {
            val geocoder = Geocoder(context, locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val first = addresses.firstOrNull()
                        if (first != null) {
                            val text = first.getAddressLine(0) ?: "${first.locality ?: ""}, ${first.countryName ?: ""}"
                            continuation.resume(ReverseGeocodeResult(text.trim(',', ' '), true))
                        } else {
                            continuation.resume(ReverseGeocodeResult(formatFallbackCoordinates(latitude, longitude), false))
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val first = addresses?.firstOrNull()
                if (first != null) {
                    val text = first.getAddressLine(0) ?: "${first.locality ?: ""}, ${first.countryName ?: ""}"
                    ReverseGeocodeResult(text.trim(',', ' '), true)
                } else {
                    ReverseGeocodeResult(formatFallbackCoordinates(latitude, longitude), false)
                }
            }
        } catch (e: Exception) {
            ReverseGeocodeResult(formatFallbackCoordinates(latitude, longitude), false)
        }
    }

    suspend fun searchPlaceByName(
        context: Context,
        query: String,
        language: Language = Language.ENGLISH,
        maxResults: Int = 5
    ): List<PlaceSearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val locale = when (language) {
            Language.HINDI -> Locale("hi", "IN")
            Language.ENGLISH -> Locale.ENGLISH
        }

        try {
            val geocoder = Geocoder(context, locale)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocationName(query, maxResults) { addresses ->
                        val results = addresses.mapNotNull { addr ->
                            val text = addr.getAddressLine(0) ?: "${addr.locality ?: ""}, ${addr.countryName ?: ""}"
                            if (text.isNotBlank()) {
                                PlaceSearchResult(text.trim(',', ' '), addr.latitude, addr.longitude)
                            } else null
                        }
                        continuation.resume(results)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, maxResults)
                addresses?.mapNotNull { addr ->
                    val text = addr.getAddressLine(0) ?: "${addr.locality ?: ""}, ${addr.countryName ?: ""}"
                    if (text.isNotBlank()) {
                        PlaceSearchResult(text.trim(',', ' '), addr.latitude, addr.longitude)
                    } else null
                } ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun formatFallbackCoordinates(lat: Double, lng: Double): String {
        val latStr = String.format(Locale.US, "%.4f", lat)
        val lngStr = String.format(Locale.US, "%.4f", lng)
        return "Coordinates ($latStr, $lngStr)"
    }
}
