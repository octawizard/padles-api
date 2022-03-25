package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.MatchResult
import com.octawizard.domain.model.Reservation
import com.octawizard.repository.reservation.ReservationRepository

class UpdateMatchResult(private val reservationRepository: ReservationRepository) {

    operator fun invoke(reservation: Reservation, matchResult: MatchResult): Reservation {
        val updatedMatch = reservation.match.copy(result = matchResult) // todo use arrows lens to make it more readable
        val updatedReservation = reservation.copy(match = updatedMatch)
        reservationRepository.updateReservation(updatedReservation)
        return updatedReservation
    }
}
