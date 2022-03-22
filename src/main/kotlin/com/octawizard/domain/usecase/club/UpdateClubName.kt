package com.octawizard.domain.usecase.club

import com.octawizard.controller.async
import com.octawizard.domain.model.Club
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.retry

class UpdateClubName(private val clubRepository: ClubRepository, val reservationRepository: ReservationRepository) {

    suspend operator fun invoke(club: Club, name: String): Club {
        clubRepository.updateClubName(club.id, name)
        // update all reservation of this club
        async { retry { reservationRepository.updateClubName(club.id, name) } }
        return club.copy(name = name)
    }
}
