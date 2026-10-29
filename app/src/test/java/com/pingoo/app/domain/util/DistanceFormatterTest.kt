package com.pingoo.app.domain.util

import com.pingoo.app.domain.model.Language
import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceFormatterTest {

    @Test
    fun testNullAndNegativeDistances() {
        assertEquals("--", DistanceFormatter.formatDistance(null, Language.ENGLISH))
        assertEquals("--", DistanceFormatter.formatDistance(-10.0, Language.ENGLISH))
        assertEquals("--", DistanceFormatter.formatDistance(Double.NaN, Language.ENGLISH))
    }

    @Test
    fun testMetersFormatting() {
        assertEquals("450 m", DistanceFormatter.formatDistance(450.0, Language.ENGLISH))
        assertEquals("450 मी.", DistanceFormatter.formatDistance(450.0, Language.HINDI))
    }

    @Test
    fun testKilometersFormatting() {
        assertEquals("12.4 km", DistanceFormatter.formatDistance(12430.0, Language.ENGLISH))
        assertEquals("12.4 कि.मी.", DistanceFormatter.formatDistance(12430.0, Language.HINDI))
    }

    @Test
    fun testAccuracyFormatting() {
        assertEquals("±15 m", DistanceFormatter.formatAccuracy(15.2f, Language.ENGLISH))
        assertEquals("±15 मी.", DistanceFormatter.formatAccuracy(15.2f, Language.HINDI))
        assertEquals("--", DistanceFormatter.formatAccuracy(null, Language.ENGLISH))
    }
}
