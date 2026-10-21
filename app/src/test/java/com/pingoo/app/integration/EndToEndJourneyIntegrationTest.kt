package com.pingoo.app.integration

import com.pingoo.app.domain.engine.MultiStopEngine
import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.GpsFix
import com.pingoo.app.domain.model.JourneyState
import com.pingoo.app.domain.model.JourneyStop
import com.pingoo.app.domain.model.MultiStopJourney
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EndToEndJourneyIntegrationTest {

    private val dest1 = Destination(id = 1, name = "New Delhi Railway Station", latitude = 28.6430, longitude = 77.2194, arrivalRadiusMeters = 500)
    private val dest2 = Destination(id = 2, name = "Anand Vihar Terminal", latitude = 28.6469, longitude = 77.3162, arrivalRadiusMeters = 500)

    private val journey = MultiStopJourney(
        id = 1,
        title = "Delhi Local Commute",
        stops = listOf(
            JourneyStop(id = 10, destination = dest1, stopOrder = 0),
            JourneyStop(id = 20, destination = dest2, stopOrder = 1)
        )
    )

    @Test
    fun testFullMultiStopJourneyEndToEndLifecycle() {
        val engine = MultiStopEngine()

        // 1. Start journey
        val startEval = engine.startJourney(journey)
        assertEquals(JourneyState.TRACKING, startEval.journeyState)
        assertEquals(0, startEval.activeStopIndex)

        // 2. Transmit noisy/inaccurate GPS fix -> should be rejected
        val noisyFix = GpsFix(latitude = 28.6430, longitude = 77.2194, accuracyMeters = 150.0f)
        val noisyEval = engine.processGpsFix(noisyFix)
        assertEquals(JourneyState.TRACKING, noisyEval.journeyState)

        // 3. Transmit accurate fix approaching Stop 1 (within 2000m warning radius)
        val warningFix = GpsFix(latitude = 28.6500, longitude = 77.2194, accuracyMeters = 10.0f)
        val warningEval = engine.processGpsFix(warningFix)
        assertEquals(JourneyState.APPROACHING, warningEval.journeyState)

        // 4. Transmit accurate fix inside Stop 1 arrival radius (within 500m)
        val arrivalFix1 = GpsFix(latitude = 28.6430, longitude = 77.2194, accuracyMeters = 5.0f)
        val arrivalEval1 = engine.processGpsFix(arrivalFix1)
        assertEquals(JourneyState.ARRIVED, arrivalEval1.journeyState)

        // 5. Acknowledge Stop 1 arrival -> Should automatically advance to Stop 2
        val advanceEval = engine.acknowledgeActiveStopAndAdvance()
        assertEquals(JourneyState.TRACKING, advanceEval.journeyState)
        assertEquals(1, advanceEval.activeStopIndex)
        assertEquals(dest2.name, advanceEval.activeStop?.destination?.name)

        // 6. Transmit accurate fix inside Stop 2 arrival radius
        val arrivalFix2 = GpsFix(latitude = 28.6469, longitude = 77.3162, accuracyMeters = 5.0f)
        val arrivalEval2 = engine.processGpsFix(arrivalFix2)
        assertEquals(JourneyState.ARRIVED, arrivalEval2.journeyState)

        // 7. Acknowledge Stop 2 arrival -> Should complete full journey
        val completionEval = engine.acknowledgeActiveStopAndAdvance()
        assertEquals(JourneyState.IDLE, completionEval.journeyState)
        assertTrue(completionEval.isJourneyCompleted)
    }

    @Test
    fun testProcessRecreationRestoration() {
        val engine1 = MultiStopEngine()
        engine1.startJourney(journey)

        // Simulate state save
        val savedActiveIndex = engine1.journey!!.activeStopIndex

        // Recreate engine instance on process restart
        val engine2 = MultiStopEngine()
        val restoredJourney = journey.copy(activeStopIndex = savedActiveIndex)
        val restoredEval = engine2.startJourney(restoredJourney)

        assertEquals(JourneyState.TRACKING, restoredEval.journeyState)
        assertEquals(savedActiveIndex, restoredEval.activeStopIndex)
    }
}
