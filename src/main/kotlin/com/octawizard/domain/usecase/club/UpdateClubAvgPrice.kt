package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.repository.club.ClubRepository
import java.math.BigDecimal

class UpdateClubAvgPrice(private val clubRepository: ClubRepository) {

    operator fun invoke(club: Club, avgPrice: BigDecimal): Club {
        clubRepository.updateClubAvgPrice(club.id, avgPrice)
        return club.copy(avgPrice = avgPrice)
    }
}
