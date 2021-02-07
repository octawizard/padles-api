package com.octawizard.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Serializable
data class Reservation(
    @Contextual val id: UUID,
    val match: Match,
    val clubReservationInfo: ClubReservationInfo,
    @Contextual val startTime: LocalDateTime,
    @Contextual val endTime: LocalDateTime,
    val reservedBy: User,
    @Contextual val price: BigDecimal,
    val status: ReservationStatus,
    val paymentStatus: PaymentStatus,
    @Contextual val createdAt: LocalDateTime = LocalDateTime.now(),
)

enum class ReservationStatus {
    Pending, Confirmed, Canceled
}

enum class PaymentStatus {
    PendingPayment, Payed, Refunded, ToBeRefunded
}

@Serializable
data class ClubReservationInfo(
    @Contextual val clubId: UUID,
    val name: String,
    val field: Field,
    val clubLocation: GeoLocation,
)

fun String.isValidUUID(): Boolean {
    return try {
        UUID.fromString(this)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}
