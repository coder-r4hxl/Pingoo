package com.pingoo.app.domain.model

data class JourneyStop(
    val id: Long = 0,
    val journeyId: Long = 0,
    val destination: Destination,
    val stopOrder: Int,
    val isAcknowledged: Boolean = false
)

data class MultiStopJourney(
    val id: Long = 0,
    val title: String,
    val stops: List<JourneyStop>,
    val activeStopIndex: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val currentStop: JourneyStop?
        get() = if (activeStopIndex in stops.indices) stops[activeStopIndex] else null

    val nextStop: JourneyStop?
        get() = if (activeStopIndex + 1 in stops.indices) stops[activeStopIndex + 1] else null
}
