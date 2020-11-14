package com.octawizard.domain.usecase.reservation

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
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetReservationTest {
    private val reservationRepository = mockk<ReservationRepository>(relaxed = true)
    private val getReservation = GetReservation(reservationRepository)

    @AfterEach
    fun `reset mocks`() {
        clearAllMocks()
    }

    @Test
    fun `GetNearestAvailableReservationsTest call repository and return a list of reservations`() {
        val expectedReservation = mockk<Reservation>()
        val reservationId = UUID.randomUUID()
        every { reservationRepository.getReservation(reservationId) } returns expectedReservation

        val reservation = getReservation.invoke(reservationId)

        assertEquals(expectedReservation, reservation)
        verify(exactly = 1) { reservationRepository.getReservation(reservationId) }
    }
}
