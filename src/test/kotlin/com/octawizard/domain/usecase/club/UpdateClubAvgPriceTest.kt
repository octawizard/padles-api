package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.EmptyAvailability
import com.octawizard.repository.club.ClubRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UpdateClubAvgPriceTest {

    @Test
    fun `UpdateClubAvgPrice call repository to update club average price`() {
        val club = Club(UUID.randomUUID(), "", "", mockk(), emptySet(), EmptyAvailability, BigDecimal.ONE, mockk())
        val repository = mockk<ClubRepository>(relaxed = true)
        val updateClubAvgPrice = UpdateClubAvgPrice(repository)

        val newAvgPrice = BigDecimal(5)
        val updatedClub = updateClubAvgPrice.invoke(club, newAvgPrice)

        assertEquals(newAvgPrice, updatedClub.avgPrice)
        verify(exactly = 1) {
            repository.updateClubAvgPrice(club.id, newAvgPrice)
        }
    }
}
