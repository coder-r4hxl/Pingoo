package com.pingoo.app.regression

import com.pingoo.app.domain.engine.AlarmManagerEngine
import com.pingoo.app.domain.engine.MultiStopEngine
import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.JourneyStop
import com.pingoo.app.domain.model.MultiStopJourney
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase6RegressionTest {

    @Test
    fun testDecimalCommaCoordinateParsing() {
        val commaLat = "28,6139".replace(',', '.').toDoubleOrNull()
        val commaLng = "77,2090".replace(',', '.').toDoubleOrNull()

        assertEquals(28.6139, commaLat!!, 0.0001)
        assertEquals(77.2090, commaLng!!, 0.0001)
    }

    @Test
    fun testRoundTripRepeatedDestinationAlertsIndependent() {
        val alarmEngine = AlarmManagerEngine()
        val destA = Destination(id = 1, name = "A", latitude = 28.61, longitude = 77.20)
        val destB = Destination(id = 2, name = "B", latitude = 28.62, longitude = 77.21)

        val keyStop1 = alarmEngine.makeOccurrenceKey(destA.id, 0, 100L)
        val keyStop2 = alarmEngine.makeOccurrenceKey(destB.id, 1, 100L)
        val keyStop3 = alarmEngine.makeOccurrenceKey(destA.id, 2, 100L)

        // Initial alert allowed for all stops
        assertTrue(alarmEngine.isArrivalAlertAllowed(keyStop1))
        assertTrue(alarmEngine.isArrivalAlertAllowed(keyStop3))

        // Acknowledge stop 1 (A)
        alarmEngine.acknowledgeArrival(keyStop1)
        assertFalse(alarmEngine.isArrivalAlertAllowed(keyStop1))

        // Stop 3 (A again in A -> B -> A) MUST still be allowed!
        assertTrue(alarmEngine.isArrivalAlertAllowed(keyStop3))
    }

    @Test
    fun testOriginalJourneyTitlePreservedInEvaluation() {
        val engine = MultiStopEngine()
        val dest = Destination(id = 1, name = "Stop 1", latitude = 28.61, longitude = 77.20)
        val journey = MultiStopJourney(
            title = "Morning Office Commute",
            stops = listOf(JourneyStop(destination = dest, stopOrder = 0))
        )

        val eval = engine.startJourney(journey)
        assertEquals("Morning Office Commute", eval.journeyTitle)

        val completedEval = engine.acknowledgeActiveStopAndAdvance()
        assertEquals("Morning Office Commute", completedEval.journeyTitle)
        assertTrue(completedEval.isJourneyCompleted)
    }
}
