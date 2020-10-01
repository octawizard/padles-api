package com.octawizard.domain.usecase.match

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.User
import com.octawizard.repository.match.MatchRepository

class JoinMatch(private val matchRepository: MatchRepository){

    operator fun invoke(user: User, match: Match): Match = matchRepository.joinMatch(user, match)
}
