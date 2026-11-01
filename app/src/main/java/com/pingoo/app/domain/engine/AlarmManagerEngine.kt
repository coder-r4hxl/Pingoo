package com.pingoo.app.domain.engine

import java.util.concurrent.ConcurrentHashMap

class AlarmManagerEngine(
    val defaultSnoozeDurationMillis: Long = 5 * 60 * 1000L // 5 minutes
) {
    // Keyed by unique stop occurrence key: "${journeyId}_${stopOrder}_${destinationId}" or destinationId.toString()
    private val acknowledgedArrivals = ConcurrentHashMap<String, Long>()
    private val snoozeTimers = ConcurrentHashMap<String, Long>()

    fun makeOccurrenceKey(destinationId: Long, stopOrder: Int = 0, journeyId: Long = 0L): String {
        return "${journeyId}_${stopOrder}_${destinationId}"
    }

    fun isArrivalAlertAllowed(
        key: String,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (acknowledgedArrivals.containsKey(key)) {
            return false
        }
        val snoozedUntil = snoozeTimers[key]
        if (snoozedUntil != null && currentTimeMillis < snoozedUntil) {
            return false
        }
        return true
    }

    fun isArrivalAlertAllowed(
        destinationId: Long,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        return isArrivalAlertAllowed(destinationId.toString(), currentTimeMillis)
    }

    fun snoozeArrival(
        key: String,
        snoozeDurationMillis: Long = defaultSnoozeDurationMillis,
        currentTimeMillis: Long = System.currentTimeMillis()
    ) {
        snoozeTimers[key] = currentTimeMillis + snoozeDurationMillis
    }

    fun snoozeArrival(
        destinationId: Long,
        snoozeDurationMillis: Long = defaultSnoozeDurationMillis,
        currentTimeMillis: Long = System.currentTimeMillis()
    ) {
        snoozeArrival(destinationId.toString(), snoozeDurationMillis, currentTimeMillis)
    }

    fun acknowledgeArrival(
        key: String,
        currentTimeMillis: Long = System.currentTimeMillis()
    ) {
        acknowledgedArrivals[key] = currentTimeMillis
        snoozeTimers.remove(key)
    }

    fun acknowledgeArrival(
        destinationId: Long,
        currentTimeMillis: Long = System.currentTimeMillis()
    ) {
        acknowledgeArrival(destinationId.toString(), currentTimeMillis)
    }

    fun resetArrivalState(key: String) {
        acknowledgedArrivals.remove(key)
        snoozeTimers.remove(key)
    }

    fun resetArrivalState(destinationId: Long) {
        resetArrivalState(destinationId.toString())
    }

    fun clearAll() {
        acknowledgedArrivals.clear()
        snoozeTimers.clear()
    }
}
