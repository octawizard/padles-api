package com.octawizard.repository.reservation

//import org.litote.kmongo.NativeMappingCategory
import com.octawizard.domain.model.ClubReservationInfo
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.PaymentStatus
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.ReservationStatus
import com.octawizard.domain.model.User
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.repository.MongoBaseTestWithUUIDRepr
import com.octawizard.repository.reservation.model.ReservationDTO
import com.octawizard.repository.reservation.model.toReservationDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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
        col.insertOne(expectedReservation.toReservationDTO())

        assertEquals(expectedReservation, repository.getReservation(reservationId))
    }

    @Test
    fun `DocumentReservationRepository should return null for a reservation that doesn't exist`() {
        assertNull(repository.getReservation(UUID.randomUUID()))
    }

}
