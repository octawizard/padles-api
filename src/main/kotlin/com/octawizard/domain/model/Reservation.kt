package com.octawizard.domain.model

import kotlinx.serialization.Contextual
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class Reservation(
        @Contextual val id: UUID,
        val match: Match,
        val clubReservationInfo: ClubReservationInfo,
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

data class ClubReservationInfo(@Contextual val clubId: UUID, val name: String, val field: Field, val clubLocation: GeoLocation)
