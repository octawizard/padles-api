package com.octawizard.repository

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.User

interface MatchRepository {

    fun createMatch(user: User): Match
    fun joinMatch(user: User, match: Match): Match
    fun allAvailableMatches(): List<Match>
}

class InMemoryMatchRepository(): MatchRepository {
    override fun createMatch(user: User): Match {
        TODO("Not yet implemented")
    }

    override fun joinMatch(user: User, match: Match): Match {
        TODO("Not yet implemented")
    }

    override fun allAvailableMatches(): List<Match> {
        TODO("Not yet implemented")
    }

}
