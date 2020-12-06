package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.retry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class UpdateClubField(
    private val clubRepository: ClubRepository,
    private val reservationRepository: ReservationRepository
) {

    operator fun invoke(
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
        GlobalScope.launch { retry { reservationRepository.updateClubField(updatedField) } }
        return updatedClub
    }

}
