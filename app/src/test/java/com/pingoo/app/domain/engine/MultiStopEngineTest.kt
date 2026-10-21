package com.pingoo.app.domain.engine

import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.GpsFix
import com.pingoo.app.domain.model.JourneyState
import com.pingoo.app.domain.model.JourneyStop
import com.pingoo.app.domain.model.MultiStopJourney
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MultiStopEngineTest {

    private val stop1 = JourneyStop(
        id = 1,
        destination = Destination(id = 10, name = "Station A", latitude = 28.6139, longitude = 77.2090, arrivalRadiusMeters = 500),
        stopOrder = 0
    )

    private val stop2 = JourneyStop(
        id = 2,
        destination = Destination(id = 20, name = "Station B", latitude = 28.6315, longitude = 77.2167, arrivalRadiusMeters = 500),
        stopOrder = 1
    )

    private val multiStopJourney = MultiStopJourney(
        id = 100,
        title = "Morning Commute",
        stops = listOf(stop1, stop2),
        activeStopIndex = 0
    )

    @Test
    fun testStartJourneyTracksFirstStop() {
        val engine = MultiStopEngine()
        val eval = engine.startJourney(multiStopJourney)

        assertEquals(JourneyState.TRACKING, eval.journeyState)
        assertEquals(0, eval.activeStopIndex)
        assertEquals(stop1, eval.activeStop)
        assertEquals(2, eval.totalStops)
        assertFalse(eval.isJourneyCompleted)
    }

    @Test
    fun testProcessGpsFixTracksCurrentActiveStop() {
        val engine = MultiStopEngine()
        engine.startJourney(multiStopJourney)

        // GPS fix near Station A (within 500m)
        val arrivalFix = GpsFix(latitude = 28.6139, longitude = 77.2090, accuracyMeters = 10.0f)
        val eval = engine.processGpsFix(arrivalFix)

        assertEquals(JourneyState.ARRIVED, eval.journeyState)
        assertEquals(stop1, eval.activeStop)
        assertEquals(0.0, eval.distanceToActiveStopMeters!!, 0.001)
    }

    @Test
    fun testAcknowledgeActiveStopAdvancesToNextStop() {
        val engine = MultiStopEngine()
        engine.startJourney(multiStopJourney)

        // Arrive at stop 1
        val arrivalFix = GpsFix(latitude = 28.6139, longitude = 77.2090, accuracyMeters = 10.0f)
        engine.processGpsFix(arrivalFix)

        // Acknowledge stop 1 and advance
        val eval = engine.acknowledgeActiveStopAndAdvance()

        assertEquals(JourneyState.TRACKING, eval.journeyState)
        assertEquals(1, eval.activeStopIndex)
        assertEquals(stop2, eval.activeStop)
        assertFalse(eval.isJourneyCompleted)
    }

    @Test
    fun testAcknowledgingFinalStopCompletesJourney() {
        val engine = MultiStopEngine()
        engine.startJourney(multiStopJourney)

        // Advance to stop 2
        engine.acknowledgeActiveStopAndAdvance()

        // Arrive at stop 2
        val stop2Fix = GpsFix(latitude = 28.6315, longitude = 77.2167, accuracyMeters = 10.0f)
        engine.processGpsFix(stop2Fix)

        // Acknowledge stop 2
        val completedEval = engine.acknowledgeActiveStopAndAdvance()

        assertEquals(JourneyState.IDLE, completedEval.journeyState)
        assertNull(completedEval.activeStop)
        assertTrue(completedEval.isJourneyCompleted)
    }

    @Test
    fun testPauseResumeAndStopJourney() {
        val engine = MultiStopEngine()
        engine.startJourney(multiStopJourney)

        val pauseEval = engine.pauseJourney()
        assertEquals(JourneyState.PAUSED, pauseEval.journeyState)

        val resumeEval = engine.resumeJourney()
        assertEquals(JourneyState.TRACKING, resumeEval.journeyState)

        val stopEval = engine.stopJourney()
        assertEquals(JourneyState.IDLE, stopEval.journeyState)
        assertNull(engine.journey)
    }
}
