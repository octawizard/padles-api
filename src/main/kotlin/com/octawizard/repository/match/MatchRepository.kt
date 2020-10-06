package com.octawizard.repository.match

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.MatchStatus
import com.octawizard.domain.model.User
import java.util.*

interface MatchRepository {

    fun createMatch(user1: User, user2: User?, user3: User?, user4: User?, matchStatus: MatchStatus): Match
    fun getMatch(id: UUID): Match?
    fun joinMatch(user: User, matchId: UUID)
    fun allAvailableMatches(): List<Match>
    fun leaveMatch(user: User, matchId: UUID)
    fun deleteMatch(matchId: UUID)
}
