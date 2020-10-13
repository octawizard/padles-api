package com.octawizard.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class Reservation(
    val id: UUID,
    val createdAt: LocalDateTime,
    val match: Match,
    val club: Club,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val reservedBy: User,
    val price: BigDecimal,
    val status: ReservationStatus,
    val paymentStatus: PaymentStatus
)

enum class ReservationStatus {
    Confirmed, Canceled
}

enum class PaymentStatus {
    PendingPayment, Payed, Refunded, ToBeRefunded
}
