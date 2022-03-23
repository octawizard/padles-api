package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.repository.club.ClubRepository
import java.time.LocalDate

class GetNearestClubs(private val clubRepository: ClubRepository) {

    operator fun invoke(
        longitude: Double,
        latitude: Double,
        radius: Double,
        radiusUnit: RadiusUnit,
    ): List<Club> {
        return clubRepository.getNearestClubs(longitude, latitude, radius, radiusUnit)
    }

    operator fun invoke(
        longitude: Double,
        latitude: Double,
        radius: Double,
        radiusUnit: RadiusUnit,
        day: LocalDate,
    ): List<Club> {
        return clubRepository.getNearestClubsAvailableForReservation(day, longitude, latitude, radius, radiusUnit)
    }
}
