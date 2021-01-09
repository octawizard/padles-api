package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.repository.club.ClubRepository

class SearchClubsByName(private val clubRepository: ClubRepository) {

    operator fun invoke(name: String): List<Club> {
        return clubRepository.searchClubsByName(name)
    }
}
