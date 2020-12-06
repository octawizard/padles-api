package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.ClubReservationInfo
import com.octawizard.domain.model.Contacts
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
import com.octawizard.repository.transaction.TransactionRepository
import com.octawizard.repository.user.UserRepository
import io.ktor.features.*
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateReservationTest {

    private val email1 = Email("test1@email.com")
    private val user1 = User(email1, "test1")
    private val email2 = Email("test2@email.com")
    private val user2 = User(email2, "test2")
    private val email3 = Email("test3@email.com")
    private val user3 = User(email3, "test3")
    private val email4 = Email("test4@email.com")
    private val user4 = User(email4, "test4")
    private val availability = mockk<Availability>(relaxed = true)
    private val fieldId = UUID.randomUUID()
    private val field = Field(fieldId, "my field", true, WallsMaterial.Glass)
    private val startTime = LocalDateTime.now()
    private val endTime = LocalDateTime.now().plusHours(2)
    private val fieldAvailability = FieldAvailability(TimeSlot(startTime, endTime), field, BigDecimal.TEN)
    private val geoLocation = GeoLocation(10.1, 10.1)
    private val club = Club(
        UUID.randomUUID(),
        "my club",
        "address",
        geoLocation,
        setOf(field),
        availability,
        BigDecimal.TEN,
        Contacts("", Email("club@gmail.com"))
    )

    private val transactionRepository = mockk<TransactionRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val clubRepository = mockk<ClubRepository>(relaxed = true)
    private val createReservation = CreateReservation(userRepository, clubRepository, transactionRepository)

    @AfterEach
    fun `reset mocks`() {
        clearAllMocks()
    }

    @Test
    fun `CreateReservation call repository to create a reservation and returns it`() {
        val match = Match(listOf(user1, user2, user3, user4))
        val clubReservationInfo = ClubReservationInfo(club.id, club.name, field, club.geoLocation)
        val expectedReservation = Reservation(
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
        every { userRepository.getUser(email1) } returns user1
        every { userRepository.getUser(email2) } returns user2
        every { userRepository.getUser(email3) } returns user3
        every { userRepository.getUser(email4) } returns user4
        every { clubRepository.getClub(club.id) } returns club
        every { availability.byDate } returns mapOf(startTime.toLocalDate() to listOf(fieldAvailability))

        every {
            transactionRepository.createReservation(
                user1,
                clubReservationInfo,
                startTime,
                endTime,
                fieldAvailability.price,
                match
            )
        } returns expectedReservation

        val createdReservation = createReservation.invoke(
            email1,
            club.id,
            fieldId,
            startTime,
            endTime,
            email2,
            email3,
            email4,
        )

        Assertions.assertEquals(expectedReservation, createdReservation)
        verify(exactly = 1) {
            transactionRepository.createReservation(
                user1,
                clubReservationInfo,
                startTime,
                endTime,
                fieldAvailability.price,
                match
            )
        }
        verify(exactly = 1) { clubRepository.getClub(club.id) }
        verify {
            userRepository.getUser(email1)
            userRepository.getUser(email2)
            userRepository.getUser(email3)
            userRepository.getUser(email4)
        }
    }

    @Test
    fun `CreateReservation throws exception creating a reservation when reservation owner does not exist`() {
        every { userRepository.getUser(email1) } returns null

        assertThrows(NotFoundException::class.java) { createReservation.invoke(
            email1,
            club.id,
            fieldId,
            startTime,
            endTime,
            email2,
            email3,
            email4,
        ) }
        verify(exactly = 1) { userRepository.getUser(email1) }
        verify { listOf(transactionRepository, clubRepository) wasNot Called }
    }

    @Test
    fun `CreateReservation throws exception creating a reservation when club does not exist`() {
        every { userRepository.getUser(email1) } returns user1
        every { clubRepository.getClub(club.id) } returns null

        assertThrows(NotFoundException::class.java) { createReservation.invoke(
            email1,
            club.id,
            fieldId,
            startTime,
            endTime,
            email2,
            email3,
            email4,
        ) }
        verify(exactly = 1) { userRepository.getUser(email1) }
        verify(exactly = 1) { clubRepository.getClub(club.id) }
        verify { transactionRepository wasNot Called }
    }

    @Test
    fun `CreateReservation throws exception creating a reservation when field does not exist in the club`() {
        every { userRepository.getUser(email1) } returns user1
        every { clubRepository.getClub(club.id) } returns club.copy(fields = emptySet())

        assertThrows(NotFoundException::class.java) { createReservation.invoke(
            email1,
            club.id,
            fieldId,
            startTime,
            endTime,
            email2,
            email3,
            email4,
        ) }
        verify(exactly = 1) { userRepository.getUser(email1) }
        verify(exactly = 1) { clubRepository.getClub(club.id) }
        verify { transactionRepository wasNot Called }
    }

    @Test
    fun `CreateReservation throws exception creating a reservation when field isn't available in the given timeslot`() {
        every { userRepository.getUser(email1) } returns user1
        every { clubRepository.getClub(club.id) } returns club

        assertThrows(IllegalArgumentException::class.java) { createReservation.invoke(
            email1,
            club.id,
            fieldId,
            startTime,
            endTime,
            email2,
            email3,
            email4,
        ) }
        verify(exactly = 1) { userRepository.getUser(email1) }
        verify(exactly = 1) { clubRepository.getClub(club.id) }
        verify { transactionRepository wasNot Called }
    }
}
