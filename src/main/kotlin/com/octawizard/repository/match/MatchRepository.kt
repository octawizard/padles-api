package com.octawizard.repository.match

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.User
import java.util.*

interface MatchRepository {

    fun createMatch(user1: User, user2: User?, user3: User?, user4: User?): Match
    fun getMatch(id: UUID): Match?
    fun joinMatch(user: User, match: Match): Match
    fun allAvailableMatches(): List<Match>
}
