package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.repository.club.ClubRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetClubTest {

    @Test
    fun `GetClub call repository to get a club`() {
        val clubId = UUID.randomUUID()
        val club = mockk<Club>()
        val repository = mockk<ClubRepository>(relaxed = true)
        val getClub = GetClub(repository)

        every { repository.getClub(clubId) } returns club

        assertEquals(club, getClub.invoke(clubId))
        verify(exactly = 1) { repository.getClub(clubId) }
    }
}
