package com.octawizard.domain.usecase.reservation

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.PaymentStatus
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.ReservationStatus
import com.octawizard.repository.reservation.ReservationRepository
import io.ktor.features.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class CancelReservation(private val reservationRepository: ReservationRepository) {

    operator fun invoke(reservationId: UUID): Reservation {
        val reservation = reservationRepository.getReservation(reservationId)
                ?: throw NotFoundException("reservation$reservationId not found")
        val now = LocalDateTime.now()
        if (now > reservation.startTime) {
            throw BadRequestException("cannot cancel a reservation for a match that should be already started")
        }
        return when (reservation.status) {
            ReservationStatus.Pending -> {
                val canceledReservation = reservation.copy(status = ReservationStatus.Canceled)
                reservationRepository.updateReservation(canceledReservation)
                canceledReservation
            }
            ReservationStatus.Confirmed -> {
                val canceledReservation = if (reservation.paymentStatus == PaymentStatus.Payed) {
                    reservation.copy(status = ReservationStatus.Canceled, paymentStatus = PaymentStatus.ToBeRefunded)
                } else {
                    reservation.copy(status = ReservationStatus.Canceled)
                }
                canceledReservation
            }
            ReservationStatus.Canceled -> throw BadRequestException("reservation $reservationId is already canceled")
        }
    }
}
