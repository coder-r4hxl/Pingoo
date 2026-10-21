package com.pingoo.app.domain.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmManagerEngineTest {

    @Test
    fun testAlertAllowedByDefault() {
        val engine = AlarmManagerEngine()
        val destinationId = 101L

        assertTrue(engine.isArrivalAlertAllowed(destinationId))
    }

    @Test
    fun testAcknowledgedArrivalSuppressesAlerts() {
        val engine = AlarmManagerEngine()
        val destinationId = 101L

        engine.acknowledgeArrival(destinationId)

        assertFalse(engine.isArrivalAlertAllowed(destinationId))
    }

    @Test
    fun testSnoozeSuppressesAlertsDuringDuration() {
        val engine = AlarmManagerEngine(defaultSnoozeDurationMillis = 300_000L) // 5 minutes
        val destinationId = 101L
        val startTime = 1_000_000L

        engine.snoozeArrival(destinationId, currentTimeMillis = startTime)

        // 2 minutes later -> Should be suppressed
        assertFalse(engine.isArrivalAlertAllowed(destinationId, currentTimeMillis = startTime + 120_000L))

        // 6 minutes later -> Should be allowed again
        assertTrue(engine.isArrivalAlertAllowed(destinationId, currentTimeMillis = startTime + 360_000L))
    }

    @Test
    fun testResetArrivalStateReenablesAlerts() {
        val engine = AlarmManagerEngine()
        val destinationId = 101L

        engine.acknowledgeArrival(destinationId)
        assertFalse(engine.isArrivalAlertAllowed(destinationId))

        engine.resetArrivalState(destinationId)
        assertTrue(engine.isArrivalAlertAllowed(destinationId))
    }
}
