package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.EmptyAvailability
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.repository.club.ClubRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AddFieldToClubTest {

    @Test
    fun `AddFieldToClub call repository to add a field to the club's ones`() {
        val club = Club(UUID.randomUUID(), "", "", mockk(), emptySet(), EmptyAvailability, BigDecimal.ONE, mockk())
        val repository = mockk<ClubRepository>(relaxed = true)
        val addFieldToClub = AddFieldToClub(repository)
        val name = "name"
        val indoor = true
        val hasSand = false
        val wallsMaterial = WallsMaterial.Glass

        val field = mockk<Field>()
        every { repository.addFieldToClub(club.id, name, indoor, hasSand, wallsMaterial) } returns field

        val updateClub = addFieldToClub.invoke(club, name, indoor, hasSand, wallsMaterial)
        assertEquals(setOf(field), updateClub.fields)
        verify(exactly = 1) { repository.addFieldToClub(club.id, name, indoor, hasSand, wallsMaterial) }
    }
}
