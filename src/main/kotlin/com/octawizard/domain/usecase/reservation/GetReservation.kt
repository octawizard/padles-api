package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.Reservation
import com.octawizard.repository.reservation.ReservationRepository
import java.util.*

class GetReservation(private val reservationRepository: ReservationRepository) {

    operator fun invoke(reservationId: UUID): Reservation? {
        return reservationRepository.getReservation(reservationId)
    }
}
