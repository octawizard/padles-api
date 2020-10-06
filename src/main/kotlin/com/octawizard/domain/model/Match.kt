package com.octawizard.domain.model

import java.time.LocalDateTime
import java.util.*

data class Match(
    val id: UUID,
    val player1: User,
    val player2: User?,
    val player3: User?,
    val player4: User?,
    val createdAt: LocalDateTime,
    val status: MatchStatus,
//    val reservation: Reservation?
    val reservationId: UUID?
)

enum class MatchStatus {
    Draft,  // missing reservation/players
    Confirmed, // reservation confirmed && players == 4
    Finished, // match has been played TODO maybe to be removed, match effective date will be inside reservation
    Canceled    // match has been canceled whenever a reservation was present
}
