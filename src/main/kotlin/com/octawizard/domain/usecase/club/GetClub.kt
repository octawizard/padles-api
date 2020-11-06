package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.repository.club.ClubRepository
import java.util.*

class GetClub(private val clubRepository: ClubRepository) {

    operator fun invoke(clubId: UUID): Club? {
        return clubRepository.getClub(clubId)
    }
}
