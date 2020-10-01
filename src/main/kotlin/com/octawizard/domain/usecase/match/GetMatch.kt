package com.octawizard.domain.usecase.match

import com.octawizard.domain.model.Match
import com.octawizard.repository.match.MatchRepository
import java.util.*

class GetMatch(private val matchRepository: MatchRepository){

    operator fun invoke(matchId: UUID): Match? = matchRepository.getMatch(matchId)
}
