package com.pingoo.app.security

import com.pingoo.app.domain.engine.AlarmManagerEngine
import com.pingoo.app.domain.history.HistoryExportHelper
import com.pingoo.app.domain.model.JourneyHistoryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityAndReliabilityTest {

    @Test
    fun testCsvFormulaInjectionSanitization() {
        val dangerousInputs = listOf(
            "=SUM(A1:A10)",
            "+12345",
            "-cmd|'/C calc'!A0",
            "@dangerousFunction()",
            "\tTAB_INJECTED"
        )

        for (input in dangerousInputs) {
            val sanitized = HistoryExportHelper.sanitizeCsvField(input)
            assertTrue("Expected leading quote for: $input", sanitized.startsWith("'"))
        }

        val normalInput = "Office Commute"
        val normalSanitized = HistoryExportHelper.sanitizeCsvField(normalInput)
        assertEquals("Office Commute", normalSanitized)
    }

    @Test
    fun testRoundTripArrivalOccurrenceIndependence() {
        val engine = AlarmManagerEngine()
        val keyStop1 = engine.makeOccurrenceKey(destinationId = 55L, stopOrder = 0, journeyId = 1L)
        val keyStop3 = engine.makeOccurrenceKey(destinationId = 55L, stopOrder = 2, journeyId = 1L)

        // Both stops are distinct occurrences of the same destination
        assertTrue(engine.isArrivalAlertAllowed(keyStop1))
        assertTrue(engine.isArrivalAlertAllowed(keyStop3))

        // Acknowledge first stop
        engine.acknowledgeArrival(keyStop1)
        assertFalse(engine.isArrivalAlertAllowed(keyStop1))

        // Second occurrence must remain active
        assertTrue(engine.isArrivalAlertAllowed(keyStop3))
    }

    @Test
    fun testServiceRestorationKeyHandling() {
        val engine = AlarmManagerEngine()
        val occurrenceKey = engine.makeOccurrenceKey(destinationId = 77L, stopOrder = 1, journeyId = 99L)
        assertEquals("99_1_77", occurrenceKey)

        engine.snoozeArrival(occurrenceKey, snoozeDurationMillis = 60_000L, currentTimeMillis = 100_000L)
        assertFalse(engine.isArrivalAlertAllowed(occurrenceKey, currentTimeMillis = 120_000L))
        assertTrue(engine.isArrivalAlertAllowed(occurrenceKey, currentTimeMillis = 170_000L))
    }
}
