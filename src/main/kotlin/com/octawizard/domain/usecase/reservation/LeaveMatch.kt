package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Reservation
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.user.UserRepository
import com.octawizard.server.route.entityNotFound

class LeaveMatch(private val reservationRepository: ReservationRepository, private val userRepository: UserRepository) {

    suspend operator fun invoke(userEmail: Email, reservation: Reservation): Reservation {
        val user = userRepository.getUser(userEmail) ?: entityNotFound(userEmail)
        // if user is the reserved, fail
        if (reservation.reservedBy.email == user.email) {
            throw IllegalArgumentException("owner cannot leave the match - cancel the reservation instead")
        }

        val updatedPlayers = reservation.match.players.filterNot { it.email == user.email }
        if (updatedPlayers == reservation.match.players) {
            return reservation
        }
        val updatedMatch = reservation.match.copy(players = updatedPlayers) // todo use arrow-kt lens
        val updatedReservation = reservation.copy(match = updatedMatch)

        reservationRepository.updateReservation(updatedReservation)
        return updatedReservation
    }
}
