package com.octawizard.domain.usecase.reservation

import com.octawizard.controller.async
import com.octawizard.domain.model.FieldAvailability
import com.octawizard.domain.model.PaymentStatus
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.ReservationStatus
import com.octawizard.domain.model.TimeSlot
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.retry
import io.ktor.features.*
import java.time.LocalDateTime
import java.util.*

class CancelReservation(
    private val reservationRepository: ReservationRepository,
    private val clubRepository: ClubRepository,
) {

    suspend operator fun invoke(reservationId: UUID): Reservation {
        val reservation = reservationRepository.getReservation(reservationId)
            ?: throw NotFoundException("reservation$reservationId not found")
        if (reservation.startTime.isBefore(LocalDateTime.now())) {
            throw BadRequestException("cannot cancel a reservation for a match that should be already started")
        }
        return kotlin.runCatching {
            val canceledReservation: Reservation = when (reservation.status) {
                ReservationStatus.Pending -> reservation.copy(status = ReservationStatus.Canceled)
                ReservationStatus.Confirmed -> {
                    if (reservation.paymentStatus == PaymentStatus.Payed) {
                        reservation.copy(
                            status = ReservationStatus.Canceled, paymentStatus = PaymentStatus.ToBeRefunded
                        )
                    } else {
                        reservation.copy(status = ReservationStatus.Canceled)
                    }
                }
                ReservationStatus.Canceled -> throw BadRequestException("reservation $reservationId is already canceled")
            }
            reservationRepository.updateReservation(canceledReservation)
            canceledReservation
        }.onSuccess {
            async {
                val fieldAvailability = FieldAvailability(
                    TimeSlot(reservation.startTime, reservation.endTime),
                    reservation.clubReservationInfo.field,
                    reservation.price
                )
                retry { clubRepository.addClubAvailability(reservation.clubReservationInfo.clubId, fieldAvailability) }
            }
        }.getOrThrow()
    }
}
