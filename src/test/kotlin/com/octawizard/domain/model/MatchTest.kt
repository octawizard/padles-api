package com.octawizard.domain.model

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MatchTest {

    @Test
    fun `Match should throw exception if no player is specified`() {
        assertThrows(IllegalStateException::class.java) { Match(emptyList()) }
    }

    @Test
    fun `Match should throw exception if number of player is over the threshold`() {
        assertThrows(IllegalStateException::class.java) {
            Match(listOf(mockk(), mockk(), mockk(), mockk(), mockk()))
        }
    }
}
