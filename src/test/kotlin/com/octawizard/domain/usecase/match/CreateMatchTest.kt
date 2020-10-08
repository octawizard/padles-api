package com.octawizard.domain.usecase.match

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.MatchStatus
import com.octawizard.domain.model.User
import com.octawizard.repository.match.MatchRepository
import com.octawizard.repository.user.UserRepository
import io.ktor.features.*
import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateMatchTest {
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val matchRepository = mockk<MatchRepository>(relaxed = true)
    private val createMatch = CreateMatch(matchRepository, userRepository)
    private val email1 = Email("test1@email.com")
    private val user1 = User(email1, "test1")
    private val email2 = Email("test2@email.com")
    private val user2 = User(email2, "test2")
    private val email3 = Email("test3@email.com")
    private val user3 = User(email3, "test3")
    private val email4 = Email("test4@email.com")
    private val user4 = User(email4, "test4")
    private val match = mockk<Match>(relaxed = true)

    @AfterEach
    fun `reset mocks`() {
        clearAllMocks()
    }

    @Test
    fun `CreateMatch call repository to create and return a match with one player`() {
        every { userRepository.getUser(email1) } returns user1
        every { matchRepository.createMatch(user1, null, null, null, MatchStatus.Draft) } returns match

        assertEquals(match, createMatch.invoke(email1, null, null, null))
        verify(exactly = 1) { userRepository.getUser(email1) }
        verify(exactly = 1) { matchRepository.createMatch(user1, null, null, null, MatchStatus.Draft) }
    }

    @Test
    fun `CreateMatch call repository to create and return a match with two players`() {
        every { userRepository.getUser(email1) } returns user1
        every { userRepository.getUser(email2) } returns user2
        every { matchRepository.createMatch(user1, user2, null, null, MatchStatus.Draft) } returns match

        assertEquals(match, createMatch.invoke(email1, email2, null, null))
        verify(exactly = 1) {
            userRepository.getUser(email1)
            userRepository.getUser(email2)
        }
        verify(exactly = 1) { matchRepository.createMatch(user1, user2, null, null, MatchStatus.Draft) }
    }

    @Test
    fun `CreateMatch call repository to create and return a match with three players`() {
        every { userRepository.getUser(email1) } returns user1
        every { userRepository.getUser(email2) } returns user2
        every { userRepository.getUser(email3) } returns user3
        every { matchRepository.createMatch(user1, user2, user3, null, MatchStatus.Draft) } returns match

        assertEquals(match, createMatch.invoke(email1, email2, email3, null))
        verify(exactly = 1) {
            userRepository.getUser(email1)
            userRepository.getUser(email2)
            userRepository.getUser(email3)
        }
        verify(exactly = 1) { matchRepository.createMatch(user1, user2, user3, null, MatchStatus.Draft) }
    }

    @Test
    fun `CreateMatch call repository to create and return a match with four players`() {
        every { userRepository.getUser(email1) } returns user1
        every { userRepository.getUser(email2) } returns user2
        every { userRepository.getUser(email3) } returns user3
        every { userRepository.getUser(email4) } returns user4
        every { matchRepository.createMatch(user1, user2, user3, user4, MatchStatus.Draft) } returns match

        assertEquals(match, createMatch.invoke(email1, email2, email3, email4))
        verify(exactly = 1) {
            userRepository.getUser(email1)
            userRepository.getUser(email2)
            userRepository.getUser(email3)
            userRepository.getUser(email4)
        }
        verify(exactly = 1) { matchRepository.createMatch(user1, user2, user3, user4, MatchStatus.Draft) }
    }

    @Test
    fun `CreateMatch throw exception when creating a match where the user creator doesn't exist`() {
        every { userRepository.getUser(email1) } returns null

        assertThrows(NotFoundException::class.java) { createMatch.invoke(email1, email2, email3, email4) }
        verify(exactly = 1) {
            userRepository.getUser(email1)
            matchRepository wasNot Called
        }
        verify(inverse = true) {
            userRepository.getUser(email2)
            userRepository.getUser(email3)
            userRepository.getUser(email4)
        }
    }
}
