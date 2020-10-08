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
class GetMatchTest {

    @Test
    fun `GetMatch call repository to get match`() {
        val repository = mockk<MatchRepository>()
        val getMatch = GetMatch(repository)
        val match = mockk<Match>(relaxed = true)
        val matchId = UUID.randomUUID()

        every { repository.getMatch(matchId) } returns match

        assertEquals(match, getMatch(matchId))
        verify(exactly = 1) { repository.getMatch(matchId ) }
    }
}
