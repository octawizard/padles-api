package com.octawizard.domain.usecase

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.User
import com.octawizard.repository.MatchRepository

class CreateMatch(private val matchRepository: MatchRepository) {

    operator fun invoke(user: User): Match = matchRepository.createMatch(user)
}
