package com.octawizard.domain.usecase.match

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.User
import com.octawizard.repository.match.MatchRepository
import java.util.*

class LeaveMatch(private val matchRepository: MatchRepository){

    operator fun invoke(user: User, matchId: UUID): Match? {
        matchRepository.leaveMatch(user, matchId)
        return matchRepository.getMatch(matchId)
    }
}
