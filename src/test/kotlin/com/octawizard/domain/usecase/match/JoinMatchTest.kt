package com.octawizard.domain.usecase.match

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.User
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
class JoinMatchTest {

    @Test
    fun `JoinMatch call repository to join a match and returns it`() {
        val repository = mockk<MatchRepository>()
        val joinMatch = JoinMatch(repository)
        val match = mockk<Match>(relaxed = true)
        val user = mockk<User>(relaxed = true)
        val matchId = UUID.randomUUID()

        every { repository.getMatch(matchId) } returns match
        every { repository.joinMatch(user, matchId) } just Runs

        assertEquals(match, joinMatch(user, matchId))
        verify(exactly = 1) {
            repository.joinMatch(user, matchId)
            repository.getMatch(matchId )
        }
    }
}
