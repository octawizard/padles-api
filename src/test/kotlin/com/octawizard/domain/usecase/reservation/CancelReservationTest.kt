package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.ClubReservationInfo
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.FieldAvailability
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.PaymentStatus
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.ReservationStatus
import com.octawizard.domain.model.TimeSlot
import com.octawizard.domain.model.User
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.reservation.ReservationRepository
import io.ktor.features.BadRequestException
import io.ktor.features.NotFoundException
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CancelReservationTest {
    private val email1 = Email("test1@email.com")
    private val user1 = User(email1, "test1")
    private val email2 = Email("test2@email.com")
    private val user2 = User(email2, "test2")
    private val email3 = Email("test3@email.com")
    private val user3 = User(email3, "test3")
    private val email4 = Email("test4@email.com")
    private val user4 = User(email4, "test4")
    private val fieldId = UUID.randomUUID()
    private val field = Field(fieldId, "my field", true, WallsMaterial.Glass)
    private val startTime = LocalDateTime.now().plusMinutes(30)
    private val endTime = LocalDateTime.now().plusHours(2)
    private val fieldAvailability = FieldAvailability(TimeSlot(startTime, endTime), field, BigDecimal.TEN)
    private val geoLocation = GeoLocation(10.1, 10.1)
    private val match = Match(listOf(user1, user2, user3, user4))
    private val clubReservationInfo = ClubReservationInfo(UUID.randomUUID(), "my club", field, geoLocation)
    private val reservation = Reservation(
        UUID.randomUUID(),
        match,
        clubReservationInfo,
        startTime,
        endTime,
        user1,
        fieldAvailability.price,
        ReservationStatus.Pending,
        PaymentStatus.PendingPayment,
    )
    private val reservationRepository = mockk<ReservationRepository>(relaxed = true)
    private val clubRepository = mockk<ClubRepository>(relaxed = true)
    private val cancelReservation = CancelReservation(reservationRepository, clubRepository)

    @AfterEach
    fun `reset mocks`() {
        clearAllMocks()
    }

    @Test
    fun `CancelReservation cancel a pending reservation and restore field availability`() {
        every { reservationRepository.getReservation(reservation.id) } returns reservation
        val fieldAvailability = FieldAvailability(
            TimeSlot(reservation.startTime, reservation.endTime),
            reservation.clubReservationInfo.field,
            reservation.price
        )
        val expectedReservation = reservation.copy(status = ReservationStatus.Canceled)

        val updatedReservation = runBlocking { cancelReservation.invoke(reservation.id) }

        assertEquals(expectedReservation, updatedReservation)
        verify(exactly = 1) {
            reservationRepository.updateReservation(expectedReservation)
            clubRepository.addClubAvailability(clubReservationInfo.clubId, fieldAvailability)
        }
    }

    @Test
    fun `CancelReservation cancel a confirmed reservation and restore field availability`() {
        every { reservationRepository.getReservation(reservation.id) } returns
                reservation.copy(status = ReservationStatus.Confirmed)
        val fieldAvailability = FieldAvailability(
            TimeSlot(reservation.startTime, reservation.endTime),
            reservation.clubReservationInfo.field,
            reservation.price
        )
        val expectedReservation =
            reservation.copy(status = ReservationStatus.Canceled)

        val updatedReservation = runBlocking { cancelReservation(reservation.id) }

        assertEquals(expectedReservation, updatedReservation)
        verify(exactly = 1) {
            reservationRepository.updateReservation(expectedReservation)
            clubRepository.addClubAvailability(clubReservationInfo.clubId, fieldAvailability)
        }
    }

    @Test
    fun `CancelReservation cancel a confirmed and payed reservation and restore field availability`() {
        every { reservationRepository.getReservation(reservation.id) } returns
                reservation.copy(status = ReservationStatus.Confirmed, paymentStatus = PaymentStatus.Payed)
        val fieldAvailability = FieldAvailability(
            TimeSlot(reservation.startTime, reservation.endTime),
            reservation.clubReservationInfo.field,
            reservation.price
        )
        val expectedReservation =
            reservation.copy(status = ReservationStatus.Canceled, paymentStatus = PaymentStatus.ToBeRefunded)

        val updatedReservation = runBlocking { cancelReservation(reservation.id) }

        assertEquals(expectedReservation, updatedReservation)
        verify(exactly = 1) {
            reservationRepository.updateReservation(expectedReservation)
            clubRepository.addClubAvailability(clubReservationInfo.clubId, fieldAvailability)
        }
    }

    @Test
    fun `CancelReservation throws exception when cancelling a reservation of a past match`() {
        every { reservationRepository.getReservation(reservation.id) } returns
                reservation.copy(startTime = reservation.startTime.minusHours(1))

        assertThrows(BadRequestException::class.java) {
            runBlocking { cancelReservation(reservation.id) }
        }
        verifyAll {
            reservationRepository.getReservation(reservation.id)
            clubRepository wasNot Called
        }
    }

    @Test
    fun `CancelReservation throws exception when cancelling a reservation that doesn't exist`() {
        every { reservationRepository.getReservation(reservation.id) } returns null
        assertThrows(NotFoundException::class.java) {
            runBlocking { cancelReservation(reservation.id) }
        }
        verifyAll {
            reservationRepository.getReservation(reservation.id)
            clubRepository wasNot Called
        }
    }

    @Test
    fun `CancelReservation throws exception when cancelling a reservation that is already cancelled`() {
        every { reservationRepository.getReservation(reservation.id) } returns
                reservation.copy(status = ReservationStatus.Canceled)

        assertThrows(BadRequestException::class.java) {
            runBlocking { cancelReservation(reservation.id) }
        }
        verifyAll {
            reservationRepository.getReservation(reservation.id)
            clubRepository wasNot Called
        }
    }
}
