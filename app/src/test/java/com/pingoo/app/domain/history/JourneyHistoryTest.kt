package com.pingoo.app.domain.history

import com.pingoo.app.domain.model.JourneyHistoryItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneyHistoryTest {

    @Test
    fun testJourneyHistoryItemCreation() {
        val item = JourneyHistoryItem(
            id = 1,
            title = "Office Commute",
            totalStops = 3,
            reachedStops = 3,
            isCompleted = true,
            durationSeconds = 1800L
        )

        assertEquals("Office Commute", item.title)
        assertEquals(3, item.totalStops)
        assertEquals(3, item.reachedStops)
        assertTrue(item.isCompleted)
        assertEquals(1800L, item.durationSeconds)
    }

    @Test
    fun testInterruptedJourneyHistoryItem() {
        val item = JourneyHistoryItem(
            id = 2,
            title = "Weekend Trip",
            totalStops = 5,
            reachedStops = 2,
            isCompleted = false,
            durationSeconds = 900L
        )

        assertEquals("Weekend Trip", item.title)
        assertEquals(5, item.totalStops)
        assertEquals(2, item.reachedStops)
        assertFalse(item.isCompleted)
    }
}
