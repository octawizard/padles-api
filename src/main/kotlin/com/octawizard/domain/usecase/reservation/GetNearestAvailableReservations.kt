package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.Reservation
import com.octawizard.repository.reservation.ReservationRepository

class GetNearestAvailableReservations(private val reservationRepository: ReservationRepository) {

    operator fun invoke(
        longitude: Double,
        latitude: Double,
        radius: Double,
        radiusUnit: RadiusUnit,
    ): List<Reservation> {
        return reservationRepository.getNearestAvailableReservations(longitude, latitude, radius, radiusUnit)
    }
}
