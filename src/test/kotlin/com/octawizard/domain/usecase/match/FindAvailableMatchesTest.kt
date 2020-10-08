package com.octawizard.domain.usecase.match

import com.octawizard.domain.model.Match
import com.octawizard.repository.match.MatchRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FindAvailableMatchesTest {

    @Test
    fun `FindAvailableMatches call repository to get available matches`() {
        val repository = mockk<MatchRepository>()
        val findAvailableMatches = FindAvailableMatches(repository)
        val matches = emptyList<Match>()

        every { repository.allAvailableMatches() } returns matches

        assertEquals(matches, findAvailableMatches())
        verify(exactly = 1) { repository.allAvailableMatches() }
    }
}
