package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.User
import com.octawizard.repository.reservation.ReservationRepository
import io.ktor.features.*

class JoinMatch(private val reservationRepository: ReservationRepository) {

    operator fun invoke(user: User, reservation: Reservation): Reservation {
        if (reservation.match.players.size == 4) {
            throw BadRequestException("match has already four players")
        }
        if (reservation.match.players.any { it.email == user.email }) {
            return reservation
        }

        val updatedMatch = reservation.match.copy(players = reservation.match.players + user) //todo use arrow-kt lens
        val updatedReservation = reservation.copy(match = updatedMatch)

        reservationRepository.updateReservation(updatedReservation)
        return updatedReservation
    }
}
