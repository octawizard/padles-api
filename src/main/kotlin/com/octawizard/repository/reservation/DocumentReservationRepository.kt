package com.octawizard.repository.reservation

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.result.UpdateResult
import com.octawizard.domain.model.ClubReservationInfo
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.PaymentStatus
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.ReservationStatus
import com.octawizard.domain.model.User
import com.octawizard.repository.reservation.model.ReservationDTO
import com.octawizard.repository.reservation.model.toClubReservationInfoDTO
import com.octawizard.repository.reservation.model.toMatchDTO
import com.octawizard.repository.reservation.model.toReservation
import com.octawizard.repository.reservation.model.toReservationDTO
import io.ktor.features.*
import org.litote.kmongo.findOneById
import org.litote.kmongo.id.StringId
import org.litote.kmongo.updateOneById
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class DocumentReservationRepository(private val reservations: MongoCollection<ReservationDTO>) : ReservationRepository {

    override fun createReservation(
        reservationOwner: User,
        clubReservationInfo: ClubReservationInfo,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        price: BigDecimal,
        match: Match
    ): Reservation {
        val reservation = ReservationDTO(
            UUID.randomUUID(),
            match.toMatchDTO(),
            clubReservationInfo.toClubReservationInfoDTO(),
            startTime,
            endTime,
            reservationOwner,
            price,
            ReservationStatus.Pending,
            PaymentStatus.PendingPayment
        )
        reservations.insertOne(reservation)
        return reservation.toReservation()
    }

    override fun getReservation(reservationId: UUID): Reservation? {
        return reservations.findOneById(reservationId)?.toReservation()
    }

    //todo check how to perform partial updates on the document and in case create several update methods
    override fun updateReservation(reservation: Reservation) {
        val result = reservations.updateOneById(
            StringId<UUID>(reservation.id.toString()),
            reservation.toReservationDTO()
        )
        val acknowledged = UpdateResult.acknowledged(result.matchedCount, result.modifiedCount, null)
        if (acknowledged.modifiedCount != 1L) {
            throw NotFoundException("reservation ${reservation.id} not found")
        }
    }

    override fun allReservationByClub(clubId: UUID): List<Reservation> {
        return reservations.find(Filters.eq("clubReservationInfo.id", clubId))
            .map { it.toReservation() }
            .toList()
    }

    override fun getNearestAvailableReservations(
        longitude: Double,
        latitude: Double,
        radius: Double,
        radiusUnit: RadiusUnit
    ): List<Reservation> {
        val filter = filterReservationsWithinSphere(longitude, latitude, radius, radiusUnit) and
                filterReservationWithMissingPlayers()
        return reservations.find(filter).map { it.toReservation() }.toList()
    }
}

