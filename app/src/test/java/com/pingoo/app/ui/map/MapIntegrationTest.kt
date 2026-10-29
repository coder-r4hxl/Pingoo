package com.pingoo.app.ui.map

import com.pingoo.app.domain.usecase.ValidateDestinationUseCase
import com.pingoo.app.domain.usecase.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapIntegrationTest {

    private val validator = ValidateDestinationUseCase()

    @Test
    fun testMapSelectedCoordinatesValidationSuccess() {
        // Map tap returns coordinates
        val selectedLat = 28.6139
        val selectedLng = 77.2090

        val result = validator(
            name = "Map Selected Location",
            latitude = selectedLat,
            longitude = selectedLng,
            arrivalRadiusMeters = 500,
            advanceWarningMeters = 2000
        )

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun testMapSelectedCoordinatesValidationFailure() {
        // Out of bound map coordinates
        val selectedLat = 95.0000
        val selectedLng = 77.2090

        val result = validator(
            name = "Invalid Map Pin",
            latitude = selectedLat,
            longitude = selectedLng,
            arrivalRadiusMeters = 500,
            advanceWarningMeters = 2000
        )

        assertTrue(result is ValidationResult.Error)
        assertEquals("Latitude must be between -90 and 90 degrees", (result as ValidationResult.Error).message)
    }
}
