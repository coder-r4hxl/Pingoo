package com.pingoo.app.domain.engine

import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.GpsFix
import com.pingoo.app.domain.model.JourneyState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArrivalStateMachineTest {

    private val destination = Destination(
        id = 1,
        name = "Connaught Place",
        latitude = 28.6315,
        longitude = 77.2167,
        arrivalRadiusMeters = 500,
        advanceWarningRadiusMeters = 2000
    )

    @Test
    fun testStartTrackingTransitionsToTracking() {
        val stateMachine = ArrivalStateMachine()
        val eval = stateMachine.startTracking(destination)

        assertEquals(JourneyState.TRACKING, eval.state)
        assertEquals(JourneyState.TRACKING, stateMachine.state)
        assertEquals(destination, stateMachine.destination)
        assertNull(eval.distanceMeters)
    }

    @Test
    fun testProcessFixTransitionsTrackingToApproachingToArrived() {
        val stateMachine = ArrivalStateMachine()
        stateMachine.startTracking(destination)

        // 1. Fix far away (~10 km away)
        val farFix = GpsFix(latitude = 28.7000, longitude = 77.2167, accuracyMeters = 10.0f)
        val farEval = stateMachine.processGpsFix(farFix)
        assertEquals(JourneyState.TRACKING, farEval.state)

        // 2. Fix approaching (~1.1 km away, within 2000m warning radius)
        val warningFix = GpsFix(latitude = 28.6415, longitude = 77.2167, accuracyMeters = 10.0f)
        val warningEval = stateMachine.processGpsFix(warningFix)
        assertEquals(JourneyState.APPROACHING, warningEval.state)

        // 3. Fix inside arrival radius (~100m away, within 500m arrival radius)
        val arrivalFix = GpsFix(latitude = 28.6324, longitude = 77.2167, accuracyMeters = 10.0f)
        val arrivalEval = stateMachine.processGpsFix(arrivalFix)
        assertEquals(JourneyState.ARRIVED, arrivalEval.state)
    }

    @Test
    fun testArrivalJustOutsideAndExactlyAtRadius() {
        val stateMachine = ArrivalStateMachine()
        stateMachine.startTracking(destination)

        // Exactly at destination (0m) -> ARRIVED
        val exactFix = GpsFix(latitude = 28.6315, longitude = 77.2167, accuracyMeters = 5.0f)
        val exactEval = stateMachine.processGpsFix(exactFix)
        assertEquals(JourneyState.ARRIVED, exactEval.state)
        assertEquals(0.0, exactEval.distanceMeters!!, 0.001)
    }

    @Test
    fun testInaccurateGpsFixIsIgnored() {
        val stateMachine = ArrivalStateMachine(maxAcceptableAccuracyMeters = 50.0f)
        stateMachine.startTracking(destination)

        val noisyFix = GpsFix(latitude = 28.6315, longitude = 77.2167, accuracyMeters = 150.0f)
        val eval = stateMachine.processGpsFix(noisyFix)

        assertEquals(JourneyState.TRACKING, eval.state)
        assertNull(stateMachine.lastFix)
    }

    @Test
    fun testStaleFixIsIgnored() {
        val stateMachine = ArrivalStateMachine(maxFixStalenessMillis = 30_000L)
        stateMachine.startTracking(destination)

        val now = System.currentTimeMillis()
        val staleFix = GpsFix(
            latitude = 28.6315,
            longitude = 77.2167,
            accuracyMeters = 10.0f,
            timestampMillis = now - 60_000L
        )
        val eval = stateMachine.processGpsFix(staleFix, currentTimeMillis = now)

        assertEquals(JourneyState.TRACKING, eval.state)
        assertNull(stateMachine.lastFix)
    }

    @Test
    fun testPauseResumeAndStopTransitions() {
        val stateMachine = ArrivalStateMachine()
        stateMachine.startTracking(destination)

        val pauseEval = stateMachine.pauseTracking()
        assertEquals(JourneyState.PAUSED, pauseEval.state)

        val resumeEval = stateMachine.resumeTracking()
        assertEquals(JourneyState.TRACKING, resumeEval.state)

        val stopEval = stateMachine.stopTracking()
        assertEquals(JourneyState.IDLE, stopEval.state)
        assertNull(stateMachine.destination)
    }

    @Test
    fun testGpsUnavailableTransition() {
        val stateMachine = ArrivalStateMachine()
        stateMachine.startTracking(destination)

        val eval = stateMachine.markGpsUnavailable("No GPS provider")
        assertEquals(JourneyState.GPS_UNAVAILABLE, eval.state)
    }

    @Test
    fun testInvalidCoordinateGpsFix() {
        val stateMachine = ArrivalStateMachine()
        stateMachine.startTracking(destination)

        val invalidFix = GpsFix(latitude = 120.0, longitude = 77.2167, accuracyMeters = 10.0f)
        val eval = stateMachine.processGpsFix(invalidFix)

        assertEquals(JourneyState.TRACKING, eval.state)
        assertNull(stateMachine.lastFix)
    }
}
