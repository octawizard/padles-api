package com.octawizard.repository.match

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.User
import java.util.*

class InMemoryMatchRepository(): MatchRepository {

    override fun createMatch(user1: User, user2: User?, user3: User?, user4: User?): Match {
        TODO("Not yet implemented")
    }

    override fun getMatch(id: UUID): Match? {
        TODO("Not yet implemented")
    }

    override fun joinMatch(user: User, match: Match): Match {
        TODO("Not yet implemented")
    }

    override fun allAvailableMatches(): List<Match> {
        TODO("Not yet implemented")
    }

}
