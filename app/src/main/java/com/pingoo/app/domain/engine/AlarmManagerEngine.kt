package com.pingoo.app.domain.engine

import java.util.concurrent.ConcurrentHashMap

class AlarmManagerEngine(
    val defaultSnoozeDurationMillis: Long = 5 * 60 * 1000L // 5 minutes
) {
    private val acknowledgedArrivals = ConcurrentHashMap<Long, Long>() // destinationId -> ackTime
    private val snoozeTimers = ConcurrentHashMap<Long, Long>() // destinationId -> snoozeUntilTime

    fun isArrivalAlertAllowed(
        destinationId: Long,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        // Check if previously acknowledged
        if (acknowledgedArrivals.containsKey(destinationId)) {
            return false
        }

        // Check if currently snoozed
        val snoozedUntil = snoozeTimers[destinationId]
        if (snoozedUntil != null && currentTimeMillis < snoozedUntil) {
            return false
        }

        return true
    }

    fun snoozeArrival(
        destinationId: Long,
        snoozeDurationMillis: Long = defaultSnoozeDurationMillis,
        currentTimeMillis: Long = System.currentTimeMillis()
    ) {
        snoozeTimers[destinationId] = currentTimeMillis + snoozeDurationMillis
    }

    fun acknowledgeArrival(
        destinationId: Long,
        currentTimeMillis: Long = System.currentTimeMillis()
    ) {
        acknowledgedArrivals[destinationId] = currentTimeMillis
        snoozeTimers.remove(destinationId)
    }

    fun resetArrivalState(destinationId: Long) {
        acknowledgedArrivals.remove(destinationId)
        snoozeTimers.remove(destinationId)
    }

    fun clearAll() {
        acknowledgedArrivals.clear()
        snoozeTimers.clear()
    }
}
