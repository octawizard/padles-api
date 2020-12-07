package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.PaymentStatus
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.ReservationStatus
import com.octawizard.domain.model.User
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.user.UserRepository
import io.ktor.features.*
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LeaveMatchTest {
    private val reservationRepository = mockk<ReservationRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val leaveMatch = LeaveMatch(reservationRepository, userRepository)

    @AfterEach
    fun `reset mocks`() {
        clearAllMocks()
    }

    @Test
    fun `LeaveMatch call repository and remove a player to the reservation match`() {
        val playerToRemove = User(Email("remove-player@test.com"), "remove player")
        val user = User(Email("test@test.com"), "")
        val reservation = Reservation(
            UUID.randomUUID(),
            Match(players = listOf(user, playerToRemove)),
            mockk(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            user,
            BigDecimal.TEN,
            ReservationStatus.Confirmed,
            PaymentStatus.PendingPayment,
        )
        val expectedReservation = reservation.copy(match = Match(players = listOf(user)))
        every { userRepository.getUser(playerToRemove.email) } returns playerToRemove

        val updatedReservation = leaveMatch(playerToRemove.email, reservation)
        assertEquals(expectedReservation, updatedReservation)

        verify(exactly = 1) { reservationRepository.updateReservation(expectedReservation) }
    }

    @Test
    fun `LeaveMatch throws exception when removing reservation owner player from a reservation`() {
        val reservation = mockk<Reservation>()
        val user = User(Email("test@test.com"), "")
        val match = Match(players = listOf(user))
        every { reservation.match } returns match
        every { reservation.reservedBy } returns user
        every { userRepository.getUser(user.email) } returns user

        assertThrows(IllegalArgumentException::class.java) {
            leaveMatch(user.email, reservation)
        }
    }

    @Test
    fun `LeaveMatch ignore removing a player to a reservation match if the player is not in the match`() {
        val expectedReservation = mockk<Reservation>()
        val otherUser = User(Email("other-user@test.com"), "other user")
        val user = User(Email("test@test.com"), "")
        every { expectedReservation.match } returns Match(players = listOf(user))
        every { expectedReservation.reservedBy } returns user
        every { userRepository.getUser(otherUser.email) } returns otherUser

        val reservation = leaveMatch(otherUser.email, expectedReservation)

        assertEquals(expectedReservation, reservation)
        verify { reservationRepository wasNot Called }
    }

    @Test
    fun `LeaveMatch throws exception when removing a player to a reservation match if player doesn't exist`() {
        val email = Email("test@test.com")
        every { userRepository.getUser(email) } returns null

        assertThrows(NotFoundException::class.java) {
            leaveMatch(email, mockk())
        }
    }
}
