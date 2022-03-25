package com.octawizard.repository.reservation

import com.octawizard.domain.model.ClubReservationInfo
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.PaymentStatus
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.ReservationStatus
import com.octawizard.domain.model.User
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.repository.MongoBaseTestWithUUIDRepr
import com.octawizard.repository.reservation.model.ReservationDTO
import com.octawizard.repository.reservation.model.toReservationDTO
import io.ktor.features.NotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.litote.kmongo.save
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DocumentReservationRepositoryTest : MongoBaseTestWithUUIDRepr<ReservationDTO>() {
    private val repository = DocumentReservationRepository(col)
    private val user = User(
        Email("test@test.com"),
        "test-user",
        createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    )
    private val field = Field(UUID.randomUUID(), "field", false, WallsMaterial.Bricks)

    @BeforeEach
    fun beforeEach() {
        col.drop()
    }

    @Test
    fun `DocumentReservationRepository should return a reservation given the id if it exists`() {
        val reservationId = UUID.randomUUID()
        val expectedReservation = Reservation(
            reservationId,
            Match(listOf(user)),
            ClubReservationInfo(UUID.randomUUID(), "club", field, GeoLocation(1.1, 2.1)),
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            user,
            BigDecimal.ONE,
            ReservationStatus.Confirmed,
            PaymentStatus.Payed,
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        )
        col.save(expectedReservation.toReservationDTO())

        assertEquals(expectedReservation, repository.getReservation(reservationId))
    }

    @Test
    fun `DocumentReservationRepository should return null for a reservation that doesn't exist`() {
        assertNull(repository.getReservation(UUID.randomUUID()))
    }

    @Test
    fun `DocumentReservationRepository should return all reservation by a club`() {
        val clubId = UUID.randomUUID()
        val expectedReservation = Reservation(
            UUID.randomUUID(),
            Match(listOf(user)),
            ClubReservationInfo(clubId, "club", field, GeoLocation(1.1, 2.1)),
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            user,
            BigDecimal.ONE,
            ReservationStatus.Confirmed,
            PaymentStatus.Payed,
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        )
        val notExpectedReservation = Reservation(
            UUID.randomUUID(),
            Match(listOf(user)),
            ClubReservationInfo(UUID.randomUUID(), "another club", field, GeoLocation(1.1, 2.1)),
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            user,
            BigDecimal.ONE,
            ReservationStatus.Confirmed,
            PaymentStatus.Payed,
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        )
        col.insertMany(listOf(expectedReservation.toReservationDTO(), notExpectedReservation.toReservationDTO()))

        assertEquals(listOf(expectedReservation), repository.allReservationByClub(clubId))
    }

    @Test
    fun `DocumentReservationRepository should update a reservation`() {
        val currentReservation = Reservation(
            UUID.randomUUID(),
            Match(listOf(user)),
            ClubReservationInfo(UUID.randomUUID(), "club", field, GeoLocation(1.1, 2.1)),
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            user,
            BigDecimal.ONE,
            ReservationStatus.Confirmed,
            PaymentStatus.PendingPayment,
            LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        )
        val expectedReservation = currentReservation.copy(paymentStatus = PaymentStatus.Payed)
        col.save(currentReservation.toReservationDTO())

        repository.updateReservation(expectedReservation)

        assertEquals(expectedReservation, repository.getReservation(expectedReservation.id))
    }

    @Test
    fun `DocumentReservationRepository should throw an exception when updating a reservation if doesn't exist`() {
        val reservation = Reservation(
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
        )
        assertThrows(NotFoundException::class.java) {
            repository.updateReservation(reservation)
        }
    }

    @Test
    fun `DocumentReservationRepository should return near and available reservations`() {
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val latitude = 41.386737
        val longitude = 2.170167
        val radius = 2.0
        val radiusUnit = RadiusUnit.Kilometers

        val farLatitude = 41.403966
        val farLongitude = 2.191595

        val nearLatitude = 41.393581
        val nearLongitude = 2.164756

        val user1 = getUser("user1")
        val user2 = getUser("user2")
        val user3 = getUser("user3")
        val user4 = getUser("user4")

        val fullMatch = Match(players = listOf(user1, user2, user3, user4))
        val notFullMatch = Match(players = listOf(user1, user2))

        val nearClubInfo =
            ClubReservationInfo(UUID.randomUUID(), "club1", field, GeoLocation(nearLongitude, nearLatitude))

        val farClubInfo =
            ClubReservationInfo(UUID.randomUUID(), "club1", field, GeoLocation(farLongitude, farLatitude))

        val farNotAvailableReservation = Reservation(
            UUID.randomUUID(),
            fullMatch,
            farClubInfo,
            now.plusHours(1),
            now.plusHours(2),
            user1,
            BigDecimal.TEN,
            ReservationStatus.Pending,
            PaymentStatus.PendingPayment,
            now,
        )
        val farAvailableReservation =
            farNotAvailableReservation.copy(match = notFullMatch, id = UUID.randomUUID())
        val nearNotAvailableReservation =
            farNotAvailableReservation.copy(clubReservationInfo = nearClubInfo, id = UUID.randomUUID())
        val nearAvailableReservation =
            nearNotAvailableReservation.copy(match = notFullMatch, id = UUID.randomUUID())
        val nearAvailablePastReservation =
            nearAvailableReservation.copy(startTime = now.minusHours(1), id = UUID.randomUUID())

        col.insertMany(
            listOf(
                farNotAvailableReservation,
                farAvailableReservation,
                nearNotAvailableReservation,
                nearAvailableReservation,
                nearAvailablePastReservation,
            ).map { it.toReservationDTO() }
        )

        assertEquals(
            listOf(nearAvailableReservation),
            repository.getNearestAvailableReservations(longitude, latitude, radius, radiusUnit)
        )
    }

    private fun getUser(name: String): User =
        User(Email("$name@test.com"), name, createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))

    @Test
    fun `DocumentReservationRepository should update all the reservation for a club with a new club name`() {
        val clubId = UUID.randomUUID()
        val clubInfo = ClubReservationInfo(clubId, "club old name", field, GeoLocation(1.1, 1.1))
        val otherClubInfo = clubInfo.copy(clubId = UUID.randomUUID(), name = "other name")
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val user = getUser("user")
        val reservation1 = Reservation(
            UUID.randomUUID(),
            Match(players = listOf(user)),
            clubInfo,
            now.plusHours(1),
            now.plusHours(2),
            user,
            BigDecimal.TEN,
            ReservationStatus.Pending,
            PaymentStatus.PendingPayment,
            now,
        )
        val reservation2 = reservation1.copy(id = UUID.randomUUID())
        val reservationOfOtherClub = reservation1.copy(id = UUID.randomUUID(), clubReservationInfo = otherClubInfo)
        col.insertMany(listOf(reservation1, reservation2, reservationOfOtherClub).map { it.toReservationDTO() })

        val newClubName = "new name"
        repository.updateClubName(clubId, newClubName)

        assertTrue(repository.allReservationByClub(clubId).all { it.clubReservationInfo.name == newClubName })
        assertTrue(
            repository.allReservationByClub(otherClubInfo.clubId).all { it.clubReservationInfo.name == "other name" }
        )
        assertEquals(2, repository.allReservationByClub(clubId).size)
        assertEquals(1, repository.allReservationByClub(otherClubInfo.clubId).size)
    }

    @Test
    fun `DocumentReservationRepository should update all the reservation for a club with a new club address`() {
        val clubId = UUID.randomUUID()
        val clubLocation = GeoLocation(1.1, 1.1)
        val clubInfo = ClubReservationInfo(clubId, "club", field, clubLocation)
        val otherClubInfo = clubInfo.copy(clubId = UUID.randomUUID(), name = "other club")
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val user = getUser("user")
        val reservation1 = Reservation(
            UUID.randomUUID(),
            Match(players = listOf(user)),
            clubInfo,
            now.plusHours(1),
            now.plusHours(2),
            user,
            BigDecimal.TEN,
            ReservationStatus.Pending,
            PaymentStatus.PendingPayment,
            now,
        )
        val reservation2 = reservation1.copy(id = UUID.randomUUID())
        val reservationOfOtherClub = reservation1.copy(id = UUID.randomUUID(), clubReservationInfo = otherClubInfo)
        col.insertMany(listOf(reservation1, reservation2, reservationOfOtherClub).map { it.toReservationDTO() })

        val newClubLocation = GeoLocation(5.1, 5.1)
        repository.updateClubAddress(clubId, newClubLocation)

        assertTrue(
            repository.allReservationByClub(clubId).all { it.clubReservationInfo.clubLocation == newClubLocation }
        )
        assertTrue(
            repository.allReservationByClub(otherClubInfo.clubId)
                .all { it.clubReservationInfo.clubLocation == clubLocation }
        )
        assertEquals(2, repository.allReservationByClub(clubId).size)
        assertEquals(1, repository.allReservationByClub(otherClubInfo.clubId).size)
    }

    @Test
    fun `DocumentReservationRepository should update all the reservation for a club with a new club field`() {
        val clubId = UUID.randomUUID()
        val clubInfo = ClubReservationInfo(clubId, "club", field, GeoLocation(1.1, 1.1))
        val otherClubInfo = clubInfo.copy(
            clubId = UUID.randomUUID(), name = "other club", field = field.copy(id = UUID.randomUUID())
        )
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val user = getUser("user")
        val reservation1 = Reservation(
            UUID.randomUUID(),
            Match(players = listOf(user)),
            clubInfo,
            now.plusHours(1),
            now.plusHours(2),
            user,
            BigDecimal.TEN,
            ReservationStatus.Pending,
            PaymentStatus.PendingPayment,
            now,
        )
        val reservation2 = reservation1.copy(id = UUID.randomUUID())
        val reservationOfOtherClub = reservation1.copy(
            id = UUID.randomUUID(), clubReservationInfo = otherClubInfo
        )
        col.insertMany(listOf(reservation1, reservation2, reservationOfOtherClub).map { it.toReservationDTO() })

        val newField = field.copy(name = "updated field", isIndoor = true)
        repository.updateClubField(newField)

        assertTrue(repository.allReservationByClub(clubId).all { it.clubReservationInfo.field == newField })
        assertTrue(
            repository.allReservationByClub(otherClubInfo.clubId)
                .all { it.clubReservationInfo.field == otherClubInfo.field }
        )
        assertEquals(2, repository.allReservationByClub(clubId).size)
        assertEquals(1, repository.allReservationByClub(otherClubInfo.clubId).size)
    }
}
