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
class JoinMatchTest {
    private val reservationRepository = mockk<ReservationRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val joinMatch = JoinMatch(reservationRepository, userRepository)

    @AfterEach
    fun `reset mocks`() {
        clearAllMocks()
    }

    @Test
    fun `JoinMatch call repository and add a player to the reservation match`() {
        val playerToAdd = User(Email("new-player@test.com"), "new player")
        val user = User(Email("test@test.com"), "")
        val match = Match(players = listOf(user, playerToAdd))
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
        val expectedReservation = reservation.copy(match = match)
        every { userRepository.getUser(playerToAdd.email) } returns playerToAdd

        val updatedReservation = joinMatch(playerToAdd.email, reservation)
        assertEquals(expectedReservation, updatedReservation)

        verify(exactly = 1) { reservationRepository.updateReservation(expectedReservation) }
    }

    @Test
    fun `JoinMatch throws exception when adding a player to a reservation match if match already has 4 players`() {
        val reservation = mockk<Reservation>()
        val match = Match(players = listOf(mockk(), mockk(), mockk(), mockk()))
        every { reservation.match } returns match

        assertThrows(BadRequestException::class.java) {
            joinMatch(mockk(), reservation)
        }
    }

    @Test
    fun `JoinMatch ignore adding a player to a reservation match if the player is already in the match`() {
        val expectedReservation = mockk<Reservation>()
        val user = User(Email("test@test.com"), "")
        val match = Match(players = listOf(user))
        every { expectedReservation.match } returns match
        every { userRepository.getUser(user.email) } returns user

        val reservation = joinMatch(user.email, expectedReservation)
        assertEquals(expectedReservation, reservation)
        verify { reservationRepository wasNot Called }
    }

    @Test
    fun `JoinMatch throws exception when adding a player to a reservation match if player doesn't exist`() {
        val email = Email("test@test.com")
        every { userRepository.getUser(email) } returns null

        assertThrows(NotFoundException::class.java) {
            joinMatch(email, mockk())
        }
    }
}
