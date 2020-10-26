package com.octawizard.repository.reservation

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.User
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

interface ReservationRepository {

    fun createReservation(reservationOwner: User, clubId: UUID, startTime: LocalDateTime, endTime: LocalDateTime,
                          price: BigDecimal, match: Match): Reservation
    fun getReservation(reservationId: UUID): Reservation?
    fun updateReservation(canceledReservation: Reservation)
}
