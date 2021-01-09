package com.octawizard.domain.usecase.club

import com.octawizard.domain.model.Club
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.repository.club.ClubRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchClubsByNameTest {
    private val repository = mockk<ClubRepository>(relaxed = true)
    private val searchClubsByName = SearchClubsByName(repository)

    @AfterEach
    fun `reset mocks`() {
        clearAllMocks()
    }

    @Test
    fun `SearchClubsByName call repository to get clubs with a similar name`() {
        val club = mockk<Club>()
        val name = "club name"

        every { repository.searchClubsByName(name) } returns listOf(club)

        assertEquals(listOf(club), searchClubsByName.invoke(name))
        verify(exactly = 1) { repository.searchClubsByName(name) }
    }
}
