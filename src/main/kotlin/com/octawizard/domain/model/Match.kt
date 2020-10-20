package com.octawizard.domain.model

import java.io.Serializable

data class Match(
    val player1: User,
    val player2: User?,
    val player3: User?,
    val player4: User?,
    val result: MatchResult?
): Serializable

data class MatchResult(val sets: List<MatchSet>): Serializable

data class MatchSet(val home: Int, val away: Int): Serializable
