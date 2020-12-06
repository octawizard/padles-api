package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.retry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UpdateClubName(private val clubRepository: ClubRepository, val reservationRepository: ReservationRepository) {

    operator fun invoke(club: Club, name: String): Club {
        clubRepository.updateClubName(club.id, name)
        // update all reservation of this club
        GlobalScope.launch { retry { reservationRepository.updateClubName(club.id, name) } }
        return club.copy(name = name)
    }
}
