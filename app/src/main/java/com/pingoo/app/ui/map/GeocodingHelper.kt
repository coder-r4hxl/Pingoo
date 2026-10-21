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
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            val first = addresses.firstOrNull()
                            if (first != null) {
                                val text = first.getAddressLine(0) ?: "${first.locality ?: ""}, ${first.countryName ?: ""}"
                                continuation.resume(ReverseGeocodeResult(text.trim(',', ' '), true))
                            } else {
                                continuation.resume(ReverseGeocodeResult(formatFallbackCoordinates(latitude, longitude), false))
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            continuation.resume(ReverseGeocodeResult(formatFallbackCoordinates(latitude, longitude), false))
                        }
                    })
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

    private fun formatFallbackCoordinates(lat: Double, lng: Double): String {
        val latStr = String.format(Locale.US, "%.4f", lat)
        val lngStr = String.format(Locale.US, "%.4f", lng)
        return "Coordinates ($latStr, $lngStr)"
    }
}
