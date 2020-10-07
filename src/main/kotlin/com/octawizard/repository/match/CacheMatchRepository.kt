package com.octawizard.repository.match

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.MatchStatus
import com.octawizard.domain.model.User
import com.octawizard.repository.RedisCache
import com.octawizard.repository.retry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class CacheMatchRepository(
        private val cache: RedisCache<UUID, Match>, private val matchRepository: MatchRepository
) : MatchRepository {

    override fun createMatch(user1: User, user2: User?, user3: User?, user4: User?, matchStatus: MatchStatus): Match {
        val match = matchRepository.createMatch(user1, user2, user3, user4, matchStatus)
        GlobalScope.launch { retry { cache.put(match.id, match) } }
        return match
    }

    override fun getMatch(id: UUID): Match? {
        fun fallbackToUserRepository(id: UUID): Match? {
            val match = matchRepository.getMatch(id)
            match?.let { GlobalScope.launch { retry { cache.put(it.id, it) } } }
            return match
        }

        return cache.get(id).takeIf { it != null } ?: fallbackToUserRepository(id)
    }

    override fun joinMatch(user: User, matchId: UUID) {
        matchRepository.joinMatch(user, matchId).also {
            runBlocking { retry { cache.delete(matchId) } }
        }
    }

    override fun allAvailableMatches(): List<Match> {
        return matchRepository.allAvailableMatches()
    }

    override fun leaveMatch(user: User, matchId: UUID) {
        matchRepository.leaveMatch(user, matchId).also {
            runBlocking { retry { cache.delete(matchId) } }
        }
    }

    override fun deleteMatch(matchId: UUID) {
        matchRepository.deleteMatch(matchId).also { runBlocking { retry { cache.delete(matchId) } } }
    }

}
