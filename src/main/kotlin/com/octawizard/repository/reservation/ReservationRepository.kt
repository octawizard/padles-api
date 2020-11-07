package com.octawizard.repository.reservation

import com.octawizard.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

interface ReservationRepository {

    fun createReservation(
        reservationOwner: User,
        clubReservationInfo: ClubReservationInfo,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        price: BigDecimal,
        match: Match,
    ): Reservation

    fun getReservation(reservationId: UUID): Reservation?
    fun updateReservation(reservation: Reservation)

    fun allReservationByClub(clubId: UUID): List<Reservation>
    fun getNearestAvailableReservations(
        longitude: Double,
        latitude: Double,
        radius: Double,
        radiusUnit: RadiusUnit,
    ): List<Reservation>

    fun updateClubName(clubId: UUID, name: String)
    fun updateClubAddress(clubId: UUID, location: GeoLocation)
    fun updateClubField(updatedField: Field)
}
