package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.EmptyAvailability
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
class UpdateClubNameTest {

    @Test
    fun `UpdateClubName call repository to update club name and update all club's reservations`() {
        val club = Club(UUID.randomUUID(), "", "", mockk(), emptySet(), EmptyAvailability, BigDecimal.ONE, mockk())
        val clubRepository = mockk<ClubRepository>(relaxed = true)
        val reservationRepository = mockk<ReservationRepository>(relaxed = true)
        val updateClubName = UpdateClubName(clubRepository, reservationRepository)

        val newName = "new club name"
        val updatedClub = runBlocking { updateClubName.invoke(club, newName) }

        assertEquals(newName, updatedClub.name)
        verify(exactly = 1) {
            clubRepository.updateClubName(club.id, newName)
            Thread.sleep(100)
            reservationRepository.updateClubName(club.id, newName)
        }
    }
}
