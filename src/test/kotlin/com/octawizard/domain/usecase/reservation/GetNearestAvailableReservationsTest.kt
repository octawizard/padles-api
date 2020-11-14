package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.Reservation
import com.octawizard.repository.reservation.ReservationRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetNearestAvailableReservationsTest {
    private val reservationRepository = mockk<ReservationRepository>(relaxed = true)
    private val getNearestAvailableReservations = GetNearestAvailableReservations(reservationRepository)

    @AfterEach
    fun `reset mocks`() {
        clearAllMocks()
    }

    @Test
    fun `GetNearestAvailableReservationsTest call repository and return a list of reservations`() {
        val longitude = 10.0
        val latitude = 5.5
        val radius = 23.1
        val radiusUnit = RadiusUnit.Miles
        every {
            reservationRepository.getNearestAvailableReservations(longitude, latitude, radius, radiusUnit)
        } returns emptyList()

        val reservations = getNearestAvailableReservations.invoke(
            longitude, latitude, radius, radiusUnit
        )

        assertEquals(emptyList<Reservation>(), reservations)
        verify(exactly = 1) {
            reservationRepository.getNearestAvailableReservations(longitude, latitude, radius, radiusUnit)
        }
    }
}
