package com.octawizard.domain.model

import java.io.Serializable

const val MATCH_MAX_NUMBER_OF_PLAYERS = 4

data class Match(val players: List<User>, val result: MatchResult? = null) : Serializable {
    init {
        check(players.isNotEmpty() && players.size <= MATCH_MAX_NUMBER_OF_PLAYERS) {
            "players should contain at least one player and less than four"
        }
    }
}

data class MatchResult(val sets: List<MatchSet>) : Serializable

data class MatchSet(val home: Int, val away: Int) : Serializable
