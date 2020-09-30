package com.octawizard.domain.usecase

import com.octawizard.domain.model.Match
import com.octawizard.repository.MatchRepository

class FindAvailableMatches(private val matchRepository: MatchRepository) {

    operator fun invoke(): List<Match> = matchRepository.allAvailableMatches()
}
