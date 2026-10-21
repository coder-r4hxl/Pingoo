package com.pingoo.app.domain.usecase

import com.pingoo.app.domain.model.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainUnitTests {

    private val validator = ValidateDestinationUseCase()

    @Test
    fun testValidDestination() {
        val result = validator(
            name = "Connaught Place",
            latitude = 28.6315,
            longitude = 77.2167,
            arrivalRadiusMeters = 500,
            advanceWarningMeters = 2000
        )
        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun testEmptyNameFails() {
        val result = validator(
            name = "   ",
            latitude = 28.6315,
            longitude = 77.2167,
            arrivalRadiusMeters = 500,
            advanceWarningMeters = 2000
        )
        assertTrue(result is ValidationResult.Error)
        assertEquals("Destination name cannot be empty", (result as ValidationResult.Error).message)
    }

    @Test
    fun testInvalidCoordinatesFail() {
        val invalidLat = validator("Test", 95.0, 77.0, 500, 2000)
        assertTrue(invalidLat is ValidationResult.Error)

        val invalidLng = validator("Test", 28.0, 185.0, 500, 2000)
        assertTrue(invalidLng is ValidationResult.Error)

        val nullLat = validator("Test", null, 77.0, 500, 2000)
        assertTrue(nullLat is ValidationResult.Error)
    }

    @Test
    fun testInvalidRadiusFail() {
        val smallRadius = validator("Test", 28.0, 77.0, 10, 2000)
        assertTrue(smallRadius is ValidationResult.Error)

        val advanceWarningTooSmall = validator("Test", 28.0, 77.0, 1000, 500)
        assertTrue(advanceWarningTooSmall is ValidationResult.Error)
    }

    @Test
    fun testLanguageCodeMapping() {
        assertEquals(Language.ENGLISH, Language.fromCode("en"))
        assertEquals(Language.HINDI, Language.fromCode("hi"))
        assertEquals(Language.HINDI, Language.fromCode("HI"))
        assertEquals(Language.ENGLISH, Language.fromCode("unknown"))
    }
}
