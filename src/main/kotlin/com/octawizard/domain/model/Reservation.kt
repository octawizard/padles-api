package com.octawizard.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class Reservation(
    val id: UUID,
    val matchId: UUID,
    val club: Club,
    val cost: BigDecimal,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val reservedBy: User,
    val status: ReservationStatus,
    val paymentStatus: PaymentStatus
)

enum class ReservationStatus {
    CONFIRMED, CANCELED
}

enum class PaymentStatus {
    PENDING_PAYMENT, PAYED
}
