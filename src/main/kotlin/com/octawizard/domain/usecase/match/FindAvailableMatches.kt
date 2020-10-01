package com.octawizard.domain.usecase.match

import com.octawizard.domain.model.Match
import com.octawizard.repository.match.MatchRepository

class FindAvailableMatches(private val matchRepository: MatchRepository) {

    operator fun invoke(): List<Match> = matchRepository.allAvailableMatches()
}
