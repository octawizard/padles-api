package com.octawizard.domain.usecase.match

import com.octawizard.repository.match.MatchRepository
import io.ktor.features.*
import java.util.*

class DeleteMatch(private val matchRepository: MatchRepository){

    operator fun invoke(matchId: UUID) {
        matchRepository.deleteMatch(matchId)
    }
}
