package com.octawizard.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class Reservation(
        val id: UUID,
        val match: Match,
        val club: Club,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val reservedBy: User,
        val price: BigDecimal,
        val status: ReservationStatus,
        val paymentStatus: PaymentStatus,
        val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class ReservationStatus {
    Pending, Confirmed, Canceled
}

enum class PaymentStatus {
    PendingPayment, Payed, Refunded, ToBeRefunded
}
