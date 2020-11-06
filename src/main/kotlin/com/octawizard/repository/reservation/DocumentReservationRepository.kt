package com.octawizard.repository.reservation

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import com.octawizard.domain.model.ClubReservationInfo
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.MATCH_MAX_NUMBER_OF_PLAYERS
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.PaymentStatus
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.ReservationStatus
import com.octawizard.domain.model.User
import com.octawizard.repository.reservation.model.ClubReservationInfoDTO
import com.octawizard.repository.reservation.model.MatchDTO
import com.octawizard.repository.reservation.model.ReservationDTO
import com.octawizard.repository.reservation.model.toClubReservationInfoDTO
import com.octawizard.repository.reservation.model.toMatchDTO
import com.octawizard.repository.reservation.model.toReservation
import com.octawizard.repository.reservation.model.toReservationDTO
import io.ktor.features.*
import org.bson.conversions.Bson
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.id.StringId
import org.litote.kmongo.lt
import org.litote.kmongo.save
import org.litote.kmongo.setTo
import org.litote.kmongo.updateOneById
import org.litote.kmongo.updateMany
import org.litote.kmongo.find
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class DocumentReservationRepository(private val reservations: MongoCollection<ReservationDTO>) : ReservationRepository {

    private val filterReservationWithMissingPlayers: Bson =
        ReservationDTO::match / MatchDTO::playersCount lt MATCH_MAX_NUMBER_OF_PLAYERS

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
        reservations.save(reservation)
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
        if (result.modifiedCount != 1L) {
            throw NotFoundException("reservation ${reservation.id} not found")
        }
    }

    override fun allReservationByClub(clubId: UUID): List<Reservation> {
        return reservations.find((ReservationDTO::clubReservationInfo / ClubReservationInfoDTO::id) eq clubId)
            .map { it.toReservation() }
            .toList()
    }

    override fun getNearestAvailableReservations(
        longitude: Double,
        latitude: Double,
        radius: Double,
        radiusUnit: RadiusUnit
    ): List<Reservation> {
        val field = ReservationDTO::clubReservationInfo / ClubReservationInfoDTO::location
        val filter = filterGeoWithinSphere(field, longitude, latitude, radius, radiusUnit) and
                filterReservationWithMissingPlayers
        return reservations.find(filter).map { it.toReservation() }.toList()
    }

    override fun updateClubName(clubId: UUID, name: String) {
        reservations.updateMany(
            (ReservationDTO::clubReservationInfo / ClubReservationInfoDTO::id) eq clubId,
            (ReservationDTO::clubReservationInfo / ClubReservationInfoDTO::name) setTo name
        )
    }

    override fun updateClubAddress(clubId: UUID, location: GeoLocation) {
        val point = Point(Position(location.longitude, location.latitude))
        reservations.updateMany(
            (ReservationDTO::clubReservationInfo / ClubReservationInfoDTO::id) eq clubId,
            (ReservationDTO::clubReservationInfo / ClubReservationInfoDTO::location) setTo point
        )
    }
}

