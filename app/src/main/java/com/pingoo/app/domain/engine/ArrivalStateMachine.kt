package com.pingoo.app.domain.engine

import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.GpsFix
import com.pingoo.app.domain.model.JourneyState
import com.pingoo.app.domain.util.HaversineDistanceCalculator
import kotlin.math.roundToInt

data class JourneyGuardianStatus(
    val isGpsHealthy: Boolean = true,
    val isStaleFixWarning: Boolean = false,
    val isWeakSignalWarning: Boolean = false,
    val guardianMessage: String = "GPS tracking healthy"
)

data class StateMachineEvaluation(
    val state: JourneyState,
    val distanceMeters: Double?,
    val statusMessage: String,
    val guardianStatus: JourneyGuardianStatus = JourneyGuardianStatus()
)

class ArrivalStateMachine(
    val maxAcceptableAccuracyMeters: Float = 100.0f,
    val maxFixStalenessMillis: Long = 60_000L, // 60 seconds
    val boundaryHysteresisMeters: Double = 20.0
) {
    private var currentState: JourneyState = JourneyState.IDLE
    private var activeDestination: Destination? = null
    private var lastValidFix: GpsFix? = null
    private var lastDistanceMeters: Double? = null

    val state: JourneyState get() = currentState
    val destination: Destination? get() = activeDestination
    val lastFix: GpsFix? get() = lastValidFix
    val lastDistance: Double? get() = lastDistanceMeters

    fun startTracking(destination: Destination): StateMachineEvaluation {
        activeDestination = destination
        currentState = JourneyState.TRACKING
        lastValidFix = null
        lastDistanceMeters = null
        return StateMachineEvaluation(
            state = currentState,
            distanceMeters = null,
            statusMessage = "Started tracking ${destination.name}",
            guardianStatus = JourneyGuardianStatus(isGpsHealthy = true, guardianMessage = "GPS initialized for ${destination.name}")
        )
    }

    fun pauseTracking(): StateMachineEvaluation {
        if (currentState != JourneyState.IDLE) {
            currentState = JourneyState.PAUSED
        }
        return StateMachineEvaluation(
            state = currentState,
            distanceMeters = lastDistanceMeters,
            statusMessage = "Tracking paused",
            guardianStatus = JourneyGuardianStatus(isGpsHealthy = true, guardianMessage = "Tracking paused by user")
        )
    }

    fun resumeTracking(): StateMachineEvaluation {
        if (currentState == JourneyState.PAUSED && activeDestination != null) {
            currentState = JourneyState.TRACKING
        }
        return StateMachineEvaluation(
            state = currentState,
            distanceMeters = lastDistanceMeters,
            statusMessage = "Tracking resumed",
            guardianStatus = JourneyGuardianStatus(isGpsHealthy = true, guardianMessage = "Tracking resumed")
        )
    }

    fun stopTracking(): StateMachineEvaluation {
        currentState = JourneyState.IDLE
        activeDestination = null
        lastValidFix = null
        lastDistanceMeters = null
        return StateMachineEvaluation(
            state = currentState,
            distanceMeters = null,
            statusMessage = "Tracking stopped",
            guardianStatus = JourneyGuardianStatus(isGpsHealthy = true, guardianMessage = "Tracking stopped")
        )
    }

    fun markGpsUnavailable(reason: String = "GPS signal lost or disabled"): StateMachineEvaluation {
        if (currentState != JourneyState.IDLE) {
            currentState = JourneyState.GPS_UNAVAILABLE
        }
        return StateMachineEvaluation(
            state = currentState,
            distanceMeters = lastDistanceMeters,
            statusMessage = reason,
            guardianStatus = JourneyGuardianStatus(isGpsHealthy = false, guardianMessage = reason)
        )
    }

    fun processGpsFix(fix: GpsFix, currentTimeMillis: Long = System.currentTimeMillis()): StateMachineEvaluation {
        if (currentState == JourneyState.IDLE || currentState == JourneyState.PAUSED) {
            return StateMachineEvaluation(
                state = currentState,
                distanceMeters = lastDistanceMeters,
                statusMessage = "State machine in $currentState state",
                guardianStatus = JourneyGuardianStatus(isGpsHealthy = true, guardianMessage = "Tracking idle/paused")
            )
        }

        val dest = activeDestination
        if (dest == null) {
            currentState = JourneyState.IDLE
            return StateMachineEvaluation(
                state = JourneyState.IDLE,
                distanceMeters = null,
                statusMessage = "No destination set",
                guardianStatus = JourneyGuardianStatus(isGpsHealthy = false, guardianMessage = "No destination configured")
            )
        }

        // Validate coordinate bounds
        if (!fix.isValidCoordinate) {
            return StateMachineEvaluation(
                state = currentState,
                distanceMeters = lastDistanceMeters,
                statusMessage = "Invalid GPS coordinates received",
                guardianStatus = JourneyGuardianStatus(isGpsHealthy = false, guardianMessage = "Invalid GPS coordinates")
            )
        }

        // Validate accuracy
        if (fix.accuracyMeters > maxAcceptableAccuracyMeters) {
            return StateMachineEvaluation(
                state = currentState,
                distanceMeters = lastDistanceMeters,
                statusMessage = "GPS accuracy poor (${fix.accuracyMeters.roundToInt()}m > ${maxAcceptableAccuracyMeters.roundToInt()}m threshold)",
                guardianStatus = JourneyGuardianStatus(
                    isGpsHealthy = false,
                    isWeakSignalWarning = true,
                    guardianMessage = "Weak GPS signal (±${fix.accuracyMeters.roundToInt()}m accuracy)"
                )
            )
        }

        // Validate staleness
        if (currentTimeMillis - fix.timestampMillis > maxFixStalenessMillis) {
            return StateMachineEvaluation(
                state = currentState,
                distanceMeters = lastDistanceMeters,
                statusMessage = "Stale GPS fix ignored",
                guardianStatus = JourneyGuardianStatus(
                    isGpsHealthy = false,
                    isStaleFixWarning = true,
                    guardianMessage = "GPS fix is stale (>60s old)"
                )
            )
        }

        // Calculate geodesic distance
        val distance = HaversineDistanceCalculator.calculateDistanceMeters(
            startLat = fix.latitude,
            startLng = fix.longitude,
            endLat = dest.latitude,
            endLng = dest.longitude
        )

        lastValidFix = fix
        lastDistanceMeters = distance

        // Transition logic with hysteresis
        val arrivalThreshold = dest.arrivalRadiusMeters.toDouble()
        val warningThreshold = dest.advanceWarningRadiusMeters.toDouble()

        when (currentState) {
            JourneyState.TRACKING, JourneyState.GPS_UNAVAILABLE -> {
                if (distance <= arrivalThreshold) {
                    currentState = JourneyState.ARRIVED
                } else if (distance <= warningThreshold) {
                    currentState = JourneyState.APPROACHING
                } else {
                    currentState = JourneyState.TRACKING
                }
            }
            JourneyState.APPROACHING -> {
                if (distance <= arrivalThreshold) {
                    currentState = JourneyState.ARRIVED
                } else if (distance > warningThreshold + boundaryHysteresisMeters) {
                    // Left approaching zone (moving away)
                    currentState = JourneyState.TRACKING
                }
            }
            JourneyState.ARRIVED -> {
                // Stay ARRIVED unless distance moves significantly beyond arrival radius + hysteresis
                if (distance > arrivalThreshold + boundaryHysteresisMeters) {
                    currentState = if (distance <= warningThreshold) JourneyState.APPROACHING else JourneyState.TRACKING
                }
            }
            else -> { /* IDLE, PAUSED handled above */ }
        }

        return StateMachineEvaluation(
            state = currentState,
            distanceMeters = distance,
            statusMessage = "Updated distance: ${distance.roundToInt()}m",
            guardianStatus = JourneyGuardianStatus(isGpsHealthy = true, guardianMessage = "GPS fix active (±${fix.accuracyMeters.roundToInt()}m)")
        )
    }
}
