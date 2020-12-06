package com.octawizard.repository.reservation.model

import com.octawizard.domain.model.PaymentStatus
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.ReservationStatus
import com.octawizard.domain.model.User
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class ReservationDTO(
    val id: UUID,
    val match: MatchDTO,
    val clubReservationInfo: ClubReservationInfoDTO,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val reservedBy: User,
    val price: BigDecimal,
    val status: ReservationStatus,
    val paymentStatus: PaymentStatus,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

fun ReservationDTO.toReservation(): Reservation {
    return Reservation(
        id, match.toMatch(), clubReservationInfo.toClubReservationInfo(), startTime, endTime, reservedBy, price, status,
        paymentStatus, createdAt
    )
}

fun ReservationDTO.toReservation(id: UUID): Reservation {
    return Reservation(
        id, match.toMatch(), clubReservationInfo.toClubReservationInfo(), startTime, endTime, reservedBy, price,
        status, paymentStatus, createdAt
    )
}

fun Reservation.toReservationDTO(): ReservationDTO {
    return ReservationDTO(
        this.id,
        this.match.toMatchDTO(),
        this.clubReservationInfo.toClubReservationInfoDTO(),
        this.startTime,
        this.endTime,
        this.reservedBy,
        this.price,
        this.status,
        this.paymentStatus,
        this.createdAt
    )
}
