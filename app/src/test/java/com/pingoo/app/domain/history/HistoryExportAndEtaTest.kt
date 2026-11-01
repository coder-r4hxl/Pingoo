package com.pingoo.app.domain.history

import com.pingoo.app.domain.model.JourneyHistoryItem
import com.pingoo.app.domain.util.DistanceFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryExportAndEtaTest {

    @Test
    fun testGenerateCsvFormatsCorrectly() {
        val items = listOf(
            JourneyHistoryItem(
                id = 1,
                title = "Home to Work",
                totalStops = 2,
                reachedStops = 2,
                isCompleted = true,
                durationSeconds = 1200,
                timestampMillis = 1700000000000L
            )
        )

        val csv = HistoryExportHelper.generateCsv(items)
        assertTrue(csv.contains("ID,Title,TotalStops,ReachedStops,Completed,DurationSeconds,DateTime"))
        assertTrue(csv.contains("1,\"Home to Work\",2,2,true,1200"))
    }

    @Test
    fun testEtaCalculationRequiresSpeedAboveThreshold() {
        // Distance 1000m, speed 10 m/s (~36 km/h) -> 100s
        val etaSecs = DistanceFormatter.calculateSpeedBasedEtaSeconds(1000.0, 10.0f)
        assertEquals(100L, etaSecs)

        // Speed <= 1.0 m/s -> null (never invent route-based ETA when stationary or creeping)
        val stationaryEta = DistanceFormatter.calculateSpeedBasedEtaSeconds(1000.0, 0.5f)
        assertNull(stationaryEta)

        val nullSpeedEta = DistanceFormatter.calculateSpeedBasedEtaSeconds(1000.0, null)
        assertNull(nullSpeedEta)
    }

    @Test
    fun testFormatEtaLocalization() {
        val etaStr = DistanceFormatter.formatEta(1200.0, 10.0f, com.pingoo.app.domain.model.Language.ENGLISH)
        assertEquals("2 min", etaStr)

        val unavailableStr = DistanceFormatter.formatEta(1200.0, 0.5f, com.pingoo.app.domain.model.Language.ENGLISH)
        assertTrue(unavailableStr.contains("Unavailable"))
    }
}
