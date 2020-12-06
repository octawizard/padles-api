package com.octawizard.repository.reservation.model

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.MatchResult
import com.octawizard.domain.model.User

data class MatchDTO(val players: List<User>, val result: MatchResult? = null, val playersCount: Int = players.size) {
    fun toMatch(): Match {
        return Match(players, result)
    }
}

fun Match.toMatchDTO(): MatchDTO {
    return MatchDTO(this.players, this.result)
}
