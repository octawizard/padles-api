package com.octawizard.repository.reservation

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.MATCH_MAX_NUMBER_OF_PLAYERS
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.Reservation
import com.octawizard.repository.reservation.model.ClubReservationInfoDTO
import com.octawizard.repository.reservation.model.MatchDTO
import com.octawizard.repository.reservation.model.ReservationDTO
import com.octawizard.repository.reservation.model.toReservation
import com.octawizard.repository.reservation.model.toReservationDTO
import io.ktor.features.NotFoundException
import org.bson.conversions.Bson
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.findOneById
import org.litote.kmongo.gt
import org.litote.kmongo.lt
import org.litote.kmongo.setTo
import org.litote.kmongo.updateMany
import org.litote.kmongo.updateOneById
import java.time.LocalDateTime
import java.util.*

class DocumentReservationRepository(private val reservations: MongoCollection<ReservationDTO>) : ReservationRepository {

    private val filterReservationWithMissingPlayers: Bson =
        ReservationDTO::match / MatchDTO::playersCount lt MATCH_MAX_NUMBER_OF_PLAYERS

    override fun getReservation(reservationId: UUID): Reservation? {
        return reservations.findOneById(reservationId)?.toReservation()
    }

    override fun updateReservation(reservation: Reservation) {
        val result = reservations.updateOneById(reservation.id, reservation.toReservationDTO())
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
        radiusUnit: RadiusUnit,
    ): List<Reservation> {
        val field = ReservationDTO::clubReservationInfo / ClubReservationInfoDTO::location
        val filter =
            filterGeoWithinSphere(
                field,
                longitude,
                latitude,
                radius,
                radiusUnit
            ) and filterReservationWithMissingPlayers and (ReservationDTO::startTime gt LocalDateTime.now())
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

    override fun updateClubField(updatedField: Field) {
        reservations.updateMany(
            (ReservationDTO::clubReservationInfo / ClubReservationInfoDTO::field / Field::id) eq updatedField.id,
            (ReservationDTO::clubReservationInfo / ClubReservationInfoDTO::field) setTo updatedField
        )
    }
}
