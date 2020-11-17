package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.MatchResult
import com.octawizard.domain.model.PaymentStatus
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.ReservationStatus
import com.octawizard.domain.model.User
import com.octawizard.repository.reservation.ReservationRepository
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UpdateMatchResultTest {
    private val reservationRepository = mockk<ReservationRepository>(relaxed = true)
    private val updateMatchResult = UpdateMatchResult(reservationRepository)

    @AfterEach
    fun `reset mocks`() {
        clearAllMocks()
    }

    @Test
    fun `UpdateMatchResult call repository to update match result and returns reservation`() {
        val user = User(Email("test@test.com"), "")
        val reservation = Reservation(
            UUID.randomUUID(),
            Match(players = listOf(user)),
            mockk(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            user,
            BigDecimal.TEN,
            ReservationStatus.Confirmed,
            PaymentStatus.PendingPayment,
        )
        val matchResult = mockk<MatchResult>()
        val match = reservation.match.copy(result = matchResult)

        val updatedReservation = updateMatchResult(reservation, matchResult)
        assertEquals(matchResult, updatedReservation.match.result)

        verify(exactly = 1) { reservationRepository.updateReservation(reservation.copy(match = match)) }
    }
}
