package com.pingoo.app.domain.engine

import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.GpsFix
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneyGuardianTest {

    private val destination = Destination(
        id = 1,
        name = "Central Station",
        latitude = 28.6139,
        longitude = 77.2090,
        arrivalRadiusMeters = 500
    )

    @Test
    fun testHealthyGpsFixStatus() {
        val stateMachine = ArrivalStateMachine()
        stateMachine.startTracking(destination)

        val fix = GpsFix(latitude = 28.6139, longitude = 77.2090, accuracyMeters = 10.0f)
        val eval = stateMachine.processGpsFix(fix)

        assertTrue(eval.guardianStatus.isGpsHealthy)
        assertFalse(eval.guardianStatus.isStaleFixWarning)
        assertFalse(eval.guardianStatus.isWeakSignalWarning)
    }

    @Test
    fun testWeakSignalWarning() {
        val stateMachine = ArrivalStateMachine(maxAcceptableAccuracyMeters = 50.0f)
        stateMachine.startTracking(destination)

        val weakFix = GpsFix(latitude = 28.6139, longitude = 77.2090, accuracyMeters = 120.0f)
        val eval = stateMachine.processGpsFix(weakFix)

        assertFalse(eval.guardianStatus.isGpsHealthy)
        assertTrue(eval.guardianStatus.isWeakSignalWarning)
    }

    @Test
    fun testStaleFixWarning() {
        val stateMachine = ArrivalStateMachine(maxFixStalenessMillis = 30_000L)
        stateMachine.startTracking(destination)

        val now = System.currentTimeMillis()
        val staleFix = GpsFix(
            latitude = 28.6139,
            longitude = 77.2090,
            accuracyMeters = 10.0f,
            timestampMillis = now - 60_000L
        )
        val eval = stateMachine.processGpsFix(staleFix, currentTimeMillis = now)

        assertFalse(eval.guardianStatus.isGpsHealthy)
        assertTrue(eval.guardianStatus.isStaleFixWarning)
    }
}
