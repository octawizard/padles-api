package com.octawizard.repository.transaction

import com.octawizard.domain.model.ClubReservationInfo
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.User
import java.math.BigDecimal
import java.time.LocalDateTime

interface TransactionRepository {
    fun createReservation(
        reservationOwner: User,
        clubReservationInfo: ClubReservationInfo,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        price: BigDecimal,
        match: Match,
    ): Reservation
}
