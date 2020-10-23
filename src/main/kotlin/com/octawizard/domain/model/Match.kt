package com.octawizard.domain.model

import java.io.Serializable

data class Match(val players: List<User>, val result: MatchResult? = null): Serializable {
    init {
        if (players.isEmpty() || players.size > 4) {
            throw IllegalArgumentException("players should contain at least one player and less than four")
        }
    }
}

data class MatchResult(val sets: List<MatchSet>): Serializable

data class MatchSet(val home: Int, val away: Int): Serializable
