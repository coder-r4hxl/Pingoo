package com.pingoo.app.domain.util

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure domain calculator for distance between geographical coordinates.
 * Independent of Android/Google APIs.
 */
object HaversineDistanceCalculator {
    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates surface distance in meters between two lat/lon coordinates in degrees.
     */
    fun calculateDistanceMeters(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Double {
        val dLat = Math.toRadians(endLat - startLat)
        val dLng = Math.toRadians(endLng - startLng)

        val lat1 = Math.toRadians(startLat)
        val lat2 = Math.toRadians(endLat)

        val a = sin(dLat / 2).pow(2.0) + sin(dLng / 2).pow(2.0) * cos(lat1) * cos(lat2)
        val c = 2 * asin(sqrt(a))

        return EARTH_RADIUS_METERS * c
    }
}
