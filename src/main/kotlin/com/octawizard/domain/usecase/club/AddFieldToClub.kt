package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.repository.club.ClubRepository

class AddFieldToClub(private val clubRepository: ClubRepository) {

    operator fun invoke(
        club: Club,
        name: String,
        indoor: Boolean,
        hasSand: Boolean,
        wallsMaterial: WallsMaterial,
    ): Club {
        val field: Field = clubRepository.addFieldToClub(club.id, name, indoor, hasSand, wallsMaterial)
        return club.copy(fields = club.fields + field)
    }
}
