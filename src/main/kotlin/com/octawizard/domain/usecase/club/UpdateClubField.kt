package com.octawizard.domain.usecase.club

import com.octawizard.controller.async
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.retry
import java.util.*

class UpdateClubField(
    private val clubRepository: ClubRepository,
    private val reservationRepository: ReservationRepository
) {

    suspend operator fun invoke(
        club: Club,
        fieldId: UUID,
        name: String,
        indoor: Boolean,
        hasSand: Boolean,
        wallsMaterial: WallsMaterial,
    ): Club {
        clubRepository.updateClubField(club.id, fieldId, name, indoor, hasSand, wallsMaterial)
        val updatedField = Field(fieldId, name, indoor, wallsMaterial, hasSand)
        val updatedFields = club.fields.map {
            if (it.id == fieldId) {
                updatedField
            } else {
                it
            }
        }.toSet()
        val updatedClub = club.copy(fields = updatedFields)
        async { retry { reservationRepository.updateClubField(updatedField) } }
        return updatedClub
    }

}
