package com.octawizard.domain.usecase.match

import com.octawizard.repository.match.MatchRepository
import io.ktor.features.*
import java.util.*

class DeleteMatch(private val matchRepository: MatchRepository){

    operator fun invoke(matchId: UUID) {
        val d = matchRepository.deleteMatch(matchId)
        if (d == 0) {
            throw NotFoundException("match $matchId not found")
        }
    }
}
