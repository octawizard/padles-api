package com.octawizard.repository.match

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.MatchStatus
import com.octawizard.domain.model.User
import com.octawizard.repository.RedisCache
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CacheMatchRepositoryTest {
    private val cache = mockk<RedisCache<UUID, Match>>(relaxed = true)
    private val matchRepository = mockk<MatchRepository>(relaxed = true)
    private val cacheMatchRepository = CacheMatchRepository(cache, matchRepository)

    @AfterEach
    fun `reset all mocks`() {
        clearAllMocks()
    }

    @Test
    fun `CacheMatchRepository should create match from repository and put it in cache`() {
        val user = User(Email("email@test.com"), "test")
        val match = mockk<Match>(relaxed = true)
        val id = UUID.randomUUID()
        every { match.id } returns id
        every { matchRepository.createMatch(any(), any(), any(), any(), any()) } returns match

        val createdMatch = cacheMatchRepository.createMatch(user, null, null, null, MatchStatus.Draft)

        verify(timeout = 50) {
            matchRepository.createMatch(user, null, null, null, MatchStatus.Draft)
            cache.put(id, createdMatch)
        }
        assertEquals(match, createdMatch)
    }

    @Test
    fun `CacheMatchRepository should return match from cache if it's present`() {
        val match = mockk<Match>(relaxed = true)
        val id = UUID.randomUUID()

        every { cache.get(id) } returns match

        val returnedMatch = cacheMatchRepository.getMatch(id)

        verify(timeout = 50) { cache.get(id) }
        verify(inverse = true) { matchRepository.getMatch(id) }
        assertEquals(match, returnedMatch)
    }

    @Test
    fun `CacheMatchRepository should return match from repository and put it in cache if it's missing from the cache`() {
        val match = mockk<Match>(relaxed = true)
        val id = UUID.randomUUID()
        every { match.id } returns id
        every { cache.get(id) } returns null
        every { matchRepository.getMatch(id) } returns match

        val returnedMatch = cacheMatchRepository.getMatch(id)

        verify(timeout = 50) {
            cache.get(id)
            matchRepository.getMatch(id)
            cache.put(id, returnedMatch!!)
        }
        assertEquals(match, returnedMatch)
    }

    @Test
    fun `CacheMatchRepository should return missing match from repository when it's missing from the cache too`() {
        every { cache.get(any()) } returns null
        every { matchRepository.getMatch(any()) } returns null

        val id = UUID.randomUUID()
        val returnedUser = cacheMatchRepository.getMatch(id)

        verify(timeout = 50) {
            cache.get(id)
            matchRepository.getMatch(id)
        }
        verify(inverse = true, timeout = 50) { cache.put(id, any()) }
        assertNull(returnedUser)
    }

    @Test
    fun `CacheMatchRepository should return available matches calling repository`() {
        cacheMatchRepository.allAvailableMatches()
        verify {
            matchRepository.allAvailableMatches()
            cache wasNot Called
        }
    }

    @Test
    fun `CacheMatchRepository should delete match if present and also from cache`() {
        val matchId = UUID.randomUUID()
        cacheMatchRepository.deleteMatch(matchId)
        verify {
            matchRepository.deleteMatch(matchId)
            cache.delete(matchId)
        }
    }

    @Test
    fun `CacheMatchRepository should join match and delete it from cache`() {
        val matchId = UUID.randomUUID()
        val user = User(Email("email@test.com"), "test")
        cacheMatchRepository.joinMatch(user, matchId)
        verify {
            matchRepository.joinMatch(user, matchId)
            cache.delete(matchId)
        }
    }

    @Test
    fun `CacheMatchRepository should leave match and delete it from cache`() {
        val matchId = UUID.randomUUID()
        val user = User(Email("email@test.com"), "test")
        cacheMatchRepository.leaveMatch(user, matchId)
        verify {
            matchRepository.leaveMatch(user, matchId)
            cache.delete(matchId)
        }
    }

}
