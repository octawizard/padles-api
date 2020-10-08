package com.octawizard.domain.usecase.match

import com.octawizard.repository.match.MatchRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeleteMatchTest {

    @Test
    fun `DeleteMatch call repository to delete match`() {
        val repository = mockk<MatchRepository>()
        val deleteMatch = DeleteMatch(repository)
        val matchId = UUID.randomUUID()

        every { repository.deleteMatch(matchId) } just Runs

        deleteMatch(matchId)
        verify(exactly = 1) { repository.deleteMatch(matchId) }
    }
}
