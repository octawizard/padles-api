package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.EmptyAvailability
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.reservation.ReservationRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UpdateClubFieldTest {

    @Test
    fun `UpdateClubField call repository to update a club field and update all reservations of that field`() {
        val field = Field(UUID.randomUUID(), "field", false, WallsMaterial.Bricks)
        val anotherField = Field(UUID.randomUUID(), "field", false, WallsMaterial.Bricks)
        val club = Club(
            UUID.randomUUID(),
            "",
            "",
            mockk(),
            setOf(field, anotherField),
            EmptyAvailability,
            BigDecimal.ONE,
            mockk()
        )
        val clubRepository = mockk<ClubRepository>(relaxed = true)
        val reservationRepository = mockk<ReservationRepository>(relaxed = true)
        val updateClubField = UpdateClubField(clubRepository, reservationRepository)

        val newFieldName = "new field name"
        val newIndoor = true
        val newHasSand = false
        val newWallsMaterial = WallsMaterial.Glass

        val updatedClub = runBlocking {
            updateClubField.invoke(
                club,
                field.id,
                newFieldName,
                newIndoor,
                newHasSand,
                newWallsMaterial,
            )
        }
        val updatedField = updatedClub.fields.find { it.id == field.id }!!
        assertEquals(newFieldName, updatedField.name)
        assertEquals(newIndoor, updatedField.isIndoor)
        assertEquals(newHasSand, updatedField.hasSand)
        assertEquals(newWallsMaterial, updatedField.wallsMaterial)
        verify(exactly = 1) {
            clubRepository.updateClubField(club.id, field.id, newFieldName, newIndoor, newHasSand, newWallsMaterial)
            Thread.sleep(100)
            reservationRepository.updateClubField(updatedField)
        }
    }
}
