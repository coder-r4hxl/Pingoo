package com.pingoo.app.domain.engine

import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.GpsFix
import com.pingoo.app.domain.model.JourneyState
import com.pingoo.app.domain.util.HaversineDistanceCalculator
import kotlin.math.roundToInt

data class StateMachineEvaluation(
    val state: JourneyState,
    val distanceMeters: Double?,
    val statusMessage: String
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
            statusMessage = "Started tracking ${destination.name}"
        )
    }

    fun pauseTracking(): StateMachineEvaluation {
        if (currentState != JourneyState.IDLE) {
            currentState = JourneyState.PAUSED
        }
        return StateMachineEvaluation(
            state = currentState,
            distanceMeters = lastDistanceMeters,
            statusMessage = "Tracking paused"
        )
    }

    fun resumeTracking(): StateMachineEvaluation {
        if (currentState == JourneyState.PAUSED && activeDestination != null) {
            currentState = JourneyState.TRACKING
        }
        return StateMachineEvaluation(
            state = currentState,
            distanceMeters = lastDistanceMeters,
            statusMessage = "Tracking resumed"
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
            statusMessage = "Tracking stopped"
        )
    }

    fun markGpsUnavailable(reason: String = "GPS signal lost or disabled"): StateMachineEvaluation {
        if (currentState != JourneyState.IDLE) {
            currentState = JourneyState.GPS_UNAVAILABLE
        }
        return StateMachineEvaluation(
            state = currentState,
            distanceMeters = lastDistanceMeters,
            statusMessage = reason
        )
    }

    fun processGpsFix(fix: GpsFix, currentTimeMillis: Long = System.currentTimeMillis()): StateMachineEvaluation {
        if (currentState == JourneyState.IDLE || currentState == JourneyState.PAUSED) {
            return StateMachineEvaluation(
                state = currentState,
                distanceMeters = lastDistanceMeters,
                statusMessage = "State machine in $currentState state"
            )
        }

        val dest = activeDestination
        if (dest == null) {
            currentState = JourneyState.IDLE
            return StateMachineEvaluation(
                state = JourneyState.IDLE,
                distanceMeters = null,
                statusMessage = "No destination set"
            )
        }

        // Validate coordinate bounds
        if (!fix.isValidCoordinate) {
            return StateMachineEvaluation(
                state = currentState,
                distanceMeters = lastDistanceMeters,
                statusMessage = "Invalid GPS coordinates received"
            )
        }

        // Validate accuracy
        if (fix.accuracyMeters > maxAcceptableAccuracyMeters) {
            return StateMachineEvaluation(
                state = currentState,
                distanceMeters = lastDistanceMeters,
                statusMessage = "GPS accuracy poor (${fix.accuracyMeters.roundToInt()}m > ${maxAcceptableAccuracyMeters.roundToInt()}m threshold)"
            )
        }

        // Validate staleness
        if (currentTimeMillis - fix.timestampMillis > maxFixStalenessMillis) {
            return StateMachineEvaluation(
                state = currentState,
                distanceMeters = lastDistanceMeters,
                statusMessage = "Stale GPS fix ignored"
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
            statusMessage = "Updated distance: ${distance.roundToInt()}m"
        )
    }
}
