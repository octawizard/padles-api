package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.User
import com.octawizard.repository.reservation.ReservationRepository

class LeaveMatch(private val reservationRepository: ReservationRepository) {

    operator fun invoke(user: User, reservation: Reservation): Reservation {
        // if user is the reserved, fail
        if (reservation.reservedBy.email == user.email) {
            throw IllegalArgumentException("owner cannot leave the match - cancel the reservation instead")
        }

        val updatedPlayers = reservation.match.players.filterNot { it.email == user.email }
        if (updatedPlayers == reservation.match.players) {
            return reservation
        }
        val updatedMatch = reservation.match.copy(players = updatedPlayers) //todo use arrow-kt lens
        val updatedReservation = reservation.copy(match = updatedMatch)

        reservationRepository.updateReservation(updatedReservation)
        return updatedReservation
    }

}
