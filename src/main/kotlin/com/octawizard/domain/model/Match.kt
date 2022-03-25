package com.octawizard.domain.model

const val MATCH_MAX_NUMBER_OF_PLAYERS = 4

data class Match(val players: List<User>, val result: MatchResult? = null) {
    init {
        check(players.isNotEmpty() && players.size <= MATCH_MAX_NUMBER_OF_PLAYERS) {
            "players should contain at least one player and less than four"
        }
    }
}

data class MatchResult(val sets: List<MatchSet>)

data class MatchSet(val home: Int, val away: Int)
