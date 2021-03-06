package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Club
import com.octawizard.repository.club.ClubRepository

class UpdateClubAvailability(private val clubRepository: ClubRepository) {

    operator fun invoke(club: Club, availability: Availability): Club {
        clubRepository.updateClubAvailability(club.id, availability)
        return club.copy(availability = availability)
    }
}
