package com.octawizard.repository.transaction

import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.ClubReservationInfo
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.EmptyAvailability
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
import com.octawizard.repository.MongoBaseTestWithUUIDRepr
import com.octawizard.repository.MongoSessionProvider
import com.octawizard.repository.club.model.ClubDTO
import com.octawizard.repository.club.model.toClubDTO
import com.octawizard.repository.reservation.model.ReservationDTO
import com.octawizard.repository.reservation.model.toReservationDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.findOneById
import org.litote.kmongo.save
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentTransactionRepositoryTest : MongoBaseTestWithUUIDRepr<ReservationDTO>(standalone = false) {

    private val reservations = col
    private val clubs = getCollection<ClubDTO>()
    private val repository = DocumentTransactionRepository(clubs, reservations, MongoSessionProvider(mongoClient))
    private val field = Field(UUID.randomUUID(), "field", false, WallsMaterial.Bricks)
    private val user = User(
        Email("test@test.com"),
        "test-user",
        createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    )

    @BeforeEach
    fun beforeEach() {

        val warmupReservation = Reservation(
            UUID.randomUUID(),
            Match(listOf(user)),
            ClubReservationInfo(UUID.randomUUID(), "club", field, GeoLocation(1.1, 2.1)),
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            user,
            BigDecimal.ONE,
            ReservationStatus.Confirmed,
            PaymentStatus.Payed,
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        ).toReservationDTO()
        val warmupClub = getClub(UUID.randomUUID()).toClubDTO()
        reservations.save(warmupReservation)
        clubs.save(warmupClub)
        reservations.deleteOneById(warmupReservation.id)
        clubs.deleteOneById(warmupClub.id)
    }

    private fun getClub(id: UUID): Club =
        Club(
            id,
            "club name",
            "club address",
            GeoLocation(1.1, 1.1),
            setOf(field),
            EmptyAvailability,
            BigDecimal.TEN,
            Contacts("21451", Email("club@test.com")),
        )

    @Test
    fun `DocumentTransactionRepository should create a reservation and update the club availability to be empty`() {
        val today = LocalDate.now()
        val startDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).withHour(10)
        val endDateTime = startDateTime.plusHours(1)
        val timeSlot = TimeSlot(startDateTime, endDateTime)
        val fieldAvailability = FieldAvailability(timeSlot, field, BigDecimal.ONE)
        val availability = Availability(mapOf(today to listOf(fieldAvailability)))

        val clubAvailable = getClub(UUID.randomUUID()).copy(availability = availability)
        clubs.save(clubAvailable.toClubDTO())
        val user = User(Email("user@test.com"), "user")
        val match = Match(listOf(user))
        val price = BigDecimal.TEN
        val clubReservationInfo =
            ClubReservationInfo(clubAvailable.id, clubAvailable.name, field, clubAvailable.geoLocation)
        val createdReservation = repository.createReservation(
            user,
            clubReservationInfo,
            startDateTime,
            endDateTime,
            price,
            match
        )

        assertEquals(match, createdReservation.match)
        assertEquals(clubReservationInfo, createdReservation.clubReservationInfo)
        assertEquals(startDateTime, createdReservation.startTime)
        assertEquals(endDateTime, createdReservation.endTime)
        assertEquals(user, createdReservation.reservedBy)
        assertEquals(price, createdReservation.price)
        assertEquals(ReservationStatus.Pending, createdReservation.status)
        assertEquals(PaymentStatus.PendingPayment, createdReservation.paymentStatus)
        assertTrue(clubs.findOneById(clubAvailable.id)!!.availability.byDate.isEmpty())
    }

    @Test
    fun `DocumentTransactionRepository should create a reservation and update the club availability for that slot`() {
        val today = LocalDate.now()
        val startDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).withHour(10)
        val endDateTime = startDateTime.plusHours(1)
        val timeSlot = TimeSlot(startDateTime, endDateTime)
        val anotherTimeSlot = TimeSlot(startDateTime.plusHours(2), endDateTime.plusHours(2))
        val fieldAvailabilityToBook = FieldAvailability(timeSlot, field, BigDecimal.ONE)
        val fieldAvailability = FieldAvailability(anotherTimeSlot, field, BigDecimal.ONE)
        val availability = Availability(mapOf(today to listOf(fieldAvailabilityToBook, fieldAvailability)))

        val clubAvailable = getClub(UUID.randomUUID()).copy(availability = availability)
        clubs.save(clubAvailable.toClubDTO())
        val user = User(Email("user@test.com"), "user")
        val match = Match(listOf(user))
        val price = BigDecimal.TEN
        val clubReservationInfo =
            ClubReservationInfo(clubAvailable.id, clubAvailable.name, field, clubAvailable.geoLocation)
        val createdReservation = repository.createReservation(
            user,
            clubReservationInfo,
            startDateTime,
            endDateTime,
            price,
            match
        )

        assertEquals(match, createdReservation.match)
        assertEquals(clubReservationInfo, createdReservation.clubReservationInfo)
        assertEquals(startDateTime, createdReservation.startTime)
        assertEquals(endDateTime, createdReservation.endTime)
        assertEquals(user, createdReservation.reservedBy)
        assertEquals(price, createdReservation.price)
        assertEquals(ReservationStatus.Pending, createdReservation.status)
        assertEquals(PaymentStatus.PendingPayment, createdReservation.paymentStatus)
        val club = clubs.findOneById(clubAvailable.id)!!
        assertEquals(1, club.availability.byDate.size)
        assertEquals(1, club.availability.byDate.values.first().size)
        assertEquals(fieldAvailability, club.availability.byDate.values.first().first())
    }

    @Test
    fun `DocumentTransactionRepository should not create a reservation and not update the club availability when field is not available in that timeSlot`() {
        val startDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).withHour(10)
        val endDateTime = startDateTime.plusHours(1)
        val clubAvailable = getClub(UUID.randomUUID())
        clubs.save(clubAvailable.toClubDTO())
        val user = User(Email("user@test.com"), "user")
        val match = Match(listOf(user))
        val price = BigDecimal.TEN
        val clubReservationInfo =
            ClubReservationInfo(clubAvailable.id, clubAvailable.name, field, clubAvailable.geoLocation)

        assertThrows(IllegalArgumentException::class.java) {
            repository.createReservation(
                user,
                clubReservationInfo,
                startDateTime,
                endDateTime,
                price,
                match
            )
        }
    }
}
