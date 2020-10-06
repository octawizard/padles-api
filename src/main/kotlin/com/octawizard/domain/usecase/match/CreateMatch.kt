package com.octawizard.domain.usecase.match

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.MatchStatus
import com.octawizard.repository.match.MatchRepository
import com.octawizard.repository.user.UserRepository
import io.ktor.features.*

class CreateMatch(private val matchRepository: MatchRepository, private val userRepository: UserRepository) {

    operator fun invoke(player1: Email, player2: Email?, player3: Email?, player4: Email?): Match {
        val user1 = userRepository.getUser(player1) ?: throw NotFoundException()
        val user2 = player2?.let { userRepository.getUser(it) }
        val user3 = player3?.let { userRepository.getUser(it) }
        val user4 = player4?.let { userRepository.getUser(it) }
        return matchRepository.createMatch(user1, user2, user3, user4, MatchStatus.Draft)
    }
}
