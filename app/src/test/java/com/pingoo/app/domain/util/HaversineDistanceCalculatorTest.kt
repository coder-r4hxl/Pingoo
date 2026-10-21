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
}
