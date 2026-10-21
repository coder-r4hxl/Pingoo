package com.pingoo.app.domain.engine

import com.pingoo.app.domain.model.GpsFix
import com.pingoo.app.domain.model.JourneyState
import com.pingoo.app.domain.model.JourneyStop
import com.pingoo.app.domain.model.MultiStopJourney

data class MultiStopEvaluation(
    val journeyState: JourneyState,
    val activeStop: JourneyStop?,
    val activeStopIndex: Int,
    val totalStops: Int,
    val distanceToActiveStopMeters: Double?,
    val statusMessage: String,
    val isJourneyCompleted: Boolean = false
)

class MultiStopEngine(
    val stateMachine: ArrivalStateMachine = ArrivalStateMachine(),
    val alarmEngine: AlarmManagerEngine = AlarmManagerEngine()
) {
    private var currentJourney: MultiStopJourney? = null

    val journey: MultiStopJourney? get() = currentJourney

    fun startJourney(journey: MultiStopJourney): MultiStopEvaluation {
        currentJourney = journey
        val firstStop = journey.currentStop

        if (firstStop == null) {
            return MultiStopEvaluation(
                journeyState = JourneyState.IDLE,
                activeStop = null,
                activeStopIndex = 0,
                totalStops = journey.stops.size,
                distanceToActiveStopMeters = null,
                statusMessage = "Journey has no stops",
                isJourneyCompleted = true
            )
        }

        val eval = stateMachine.startTracking(firstStop.destination)
        return MultiStopEvaluation(
            journeyState = eval.state,
            activeStop = firstStop,
            activeStopIndex = journey.activeStopIndex,
            totalStops = journey.stops.size,
            distanceToActiveStopMeters = eval.distanceMeters,
            statusMessage = "Started tracking stop ${journey.activeStopIndex + 1}/${journey.stops.size}: ${firstStop.destination.name}"
        )
    }

    fun processGpsFix(fix: GpsFix, currentTimeMillis: Long = System.currentTimeMillis()): MultiStopEvaluation {
        val activeJourney = currentJourney ?: return MultiStopEvaluation(
            journeyState = JourneyState.IDLE,
            activeStop = null,
            activeStopIndex = 0,
            totalStops = 0,
            distanceToActiveStopMeters = null,
            statusMessage = "No active journey"
        )

        val activeStop = activeJourney.currentStop ?: return MultiStopEvaluation(
            journeyState = JourneyState.IDLE,
            activeStop = null,
            activeStopIndex = activeJourney.activeStopIndex,
            totalStops = activeJourney.stops.size,
            distanceToActiveStopMeters = null,
            statusMessage = "Journey completed",
            isJourneyCompleted = true
        )

        val eval = stateMachine.processGpsFix(fix, currentTimeMillis)

        return MultiStopEvaluation(
            journeyState = eval.state,
            activeStop = activeStop,
            activeStopIndex = activeJourney.activeStopIndex,
            totalStops = activeJourney.stops.size,
            distanceToActiveStopMeters = eval.distanceMeters,
            statusMessage = eval.statusMessage
        )
    }

    fun acknowledgeActiveStopAndAdvance(): MultiStopEvaluation {
        val activeJourney = currentJourney ?: return MultiStopEvaluation(
            journeyState = JourneyState.IDLE,
            activeStop = null,
            activeStopIndex = 0,
            totalStops = 0,
            distanceToActiveStopMeters = null,
            statusMessage = "No active journey to advance"
        )

        val activeStop = activeJourney.currentStop
        if (activeStop != null) {
            alarmEngine.acknowledgeArrival(activeStop.destination.id)
        }

        val nextIndex = activeJourney.activeStopIndex + 1
        if (nextIndex < activeJourney.stops.size) {
            val updatedJourney = activeJourney.copy(activeStopIndex = nextIndex)
            currentJourney = updatedJourney
            val nextStop = updatedJourney.currentStop!!

            val eval = stateMachine.startTracking(nextStop.destination)
            return MultiStopEvaluation(
                journeyState = eval.state,
                activeStop = nextStop,
                activeStopIndex = nextIndex,
                totalStops = updatedJourney.stops.size,
                distanceToActiveStopMeters = null,
                statusMessage = "Advanced to stop ${nextIndex + 1}/${updatedJourney.stops.size}: ${nextStop.destination.name}"
            )
        } else {
            val completedJourney = activeJourney.copy(isCompleted = true)
            currentJourney = completedJourney
            stateMachine.stopTracking()

            return MultiStopEvaluation(
                journeyState = JourneyState.IDLE,
                activeStop = null,
                activeStopIndex = activeJourney.stops.size,
                totalStops = activeJourney.stops.size,
                distanceToActiveStopMeters = null,
                statusMessage = "Journey Completed! All stops reached.",
                isJourneyCompleted = true
            )
        }
    }

    fun pauseJourney(): MultiStopEvaluation {
        val eval = stateMachine.pauseTracking()
        val activeJourney = currentJourney
        return MultiStopEvaluation(
            journeyState = eval.state,
            activeStop = activeJourney?.currentStop,
            activeStopIndex = activeJourney?.activeStopIndex ?: 0,
            totalStops = activeJourney?.stops?.size ?: 0,
            distanceToActiveStopMeters = eval.distanceMeters,
            statusMessage = "Journey Paused"
        )
    }

    fun resumeJourney(): MultiStopEvaluation {
        val eval = stateMachine.resumeTracking()
        val activeJourney = currentJourney
        return MultiStopEvaluation(
            journeyState = eval.state,
            activeStop = activeJourney?.currentStop,
            activeStopIndex = activeJourney?.activeStopIndex ?: 0,
            totalStops = activeJourney?.stops?.size ?: 0,
            distanceToActiveStopMeters = eval.distanceMeters,
            statusMessage = "Journey Resumed"
        )
    }

    fun stopJourney(): MultiStopEvaluation {
        stateMachine.stopTracking()
        alarmEngine.clearAll()
        val total = currentJourney?.stops?.size ?: 0
        currentJourney = null

        return MultiStopEvaluation(
            journeyState = JourneyState.IDLE,
            activeStop = null,
            activeStopIndex = 0,
            totalStops = total,
            distanceToActiveStopMeters = null,
            statusMessage = "Journey Stopped"
        )
    }
}
