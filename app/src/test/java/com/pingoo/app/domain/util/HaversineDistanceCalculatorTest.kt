package com.pingoo.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HaversineDistanceCalculatorTest {

    @Test
    fun testSameLocationReturnsZeroDistance() {
        val distance = HaversineDistanceCalculator.calculateDistanceMeters(
            startLat = 28.6139,
            startLng = 77.2090,
            endLat = 28.6139,
            endLng = 77.2090
        )
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun testKnownDistanceNewDelhiToMumbai() {
        // New Delhi (28.6139, 77.2090) to Mumbai (19.0760, 72.8777) ~ 1148 km
        val distance = HaversineDistanceCalculator.calculateDistanceMeters(
            startLat = 28.6139,
            startLng = 77.2090,
            endLat = 19.0760,
            endLng = 72.8777
        )
        // Expect approximately 1,148,000 meters (+/- 10 km tolerances due to Earth curvature model)
        assertTrue("Distance should be roughly 1148km, got: $distance", distance in 1140000.0..1160000.0)
    }

    @Test
    fun testShortDistance() {
        // ~111 meters is roughly 0.001 degrees latitude difference
        val distance = HaversineDistanceCalculator.calculateDistanceMeters(
            startLat = 28.6139,
            startLng = 77.2090,
            endLat = 28.6149,
            endLng = 77.2090
        )
        assertTrue("Short distance should be around 111 meters, got $distance", distance in 100.0..120.0)
    }

    @Test
    fun testAntipodalAndExtremeCoordinates() {
        // Equator to North Pole (0,0 to 90,0) ~ 10,001 km
        val equatorToPole = HaversineDistanceCalculator.calculateDistanceMeters(
            startLat = 0.0,
            startLng = 0.0,
            endLat = 90.0,
            endLng = 0.0
        )
        assertTrue("Equator to Pole should be ~10,000,000m, got: $equatorToPole", equatorToPole in 9990000.0..10020000.0)

        // Across Date Line (0, 179.0 to 0, -179.0) ~ 222 km
        val acrossDateLine = HaversineDistanceCalculator.calculateDistanceMeters(
            startLat = 0.0,
            startLng = 179.0,
            endLat = 0.0,
            endLng = -179.0
        )
        assertTrue("Across Date Line should be ~222,000m, got: $acrossDateLine", acrossDateLine in 220000.0..225000.0)
    }
}
