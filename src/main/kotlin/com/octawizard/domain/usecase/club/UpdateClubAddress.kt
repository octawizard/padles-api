package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.GeoLocation
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.retry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UpdateClubAddress(private val clubRepository: ClubRepository, val reservationRepository: ReservationRepository) {

    operator fun invoke(club: Club, address: String, location: GeoLocation): Club {
        clubRepository.updateClubAddress(club.id, address, location)
        // update all reservation of this club
        GlobalScope.launch { retry { reservationRepository.updateClubAddress(club.id, location) } }
        return club.copy(address = address, geoLocation = location)
    }
}
