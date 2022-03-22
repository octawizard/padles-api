package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Reservation
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.user.UserRepository
import com.octawizard.server.route.entityNotFound
import io.ktor.features.*

class JoinMatch(private val reservationRepository: ReservationRepository, private val userRepository: UserRepository) {

    suspend operator fun invoke(userEmail: Email, reservation: Reservation): Reservation {
        val user = userRepository.getUser(userEmail) ?: entityNotFound(userEmail)
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
