package com.octawizard.controller.reservation

import com.octawizard.controller.async
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.MatchResult
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.usecase.reservation.CancelReservation
import com.octawizard.domain.usecase.reservation.CreateReservation
import com.octawizard.domain.usecase.reservation.GetNearestAvailableReservations
import com.octawizard.domain.usecase.reservation.GetReservation
import com.octawizard.domain.usecase.reservation.JoinMatch
import com.octawizard.domain.usecase.reservation.LeaveMatch
import com.octawizard.domain.usecase.reservation.UpdateMatchResult
import com.octawizard.server.input.OpType
import com.octawizard.server.input.PatchMatchInput
import java.time.LocalDateTime
import java.util.*

class ReservationController(
    // Reservation
    private val createReservation: CreateReservation,
    private val cancelReservation: CancelReservation,
    private val getReservation: GetReservation,
    private val getNearestAvailableReservations: GetNearestAvailableReservations,
    // Reservation Match
    private val updateMatchResult: UpdateMatchResult,
    private val joinMatch: JoinMatch,
    private val leaveMatch: LeaveMatch,
) {

    suspend fun getReservation(reservationId: UUID): Reservation? {
        return async { getReservation.invoke(reservationId) }
    }

    suspend fun createReservation(
        reservedBy: Email,
        clubId: UUID,
        fieldId: UUID,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        matchEmailPlayer2: Email?,
        matchEmailPlayer3: Email?,
        matchEmailPlayer4: Email?,
    ): Reservation {
        return async {
            createReservation.invoke(
                reservedBy, clubId, fieldId, startTime, endTime, matchEmailPlayer2, matchEmailPlayer3, matchEmailPlayer4
            )
        }
    }

    suspend fun cancelReservation(reservation: Reservation): Reservation {
        return async { cancelReservation.invoke(reservation.id) }
    }

    suspend fun updateReservationMatchResult(reservation: Reservation, matchResult: MatchResult): Reservation {
        return async { updateMatchResult.invoke(reservation, matchResult) }
    }

    suspend fun getNearestAvailableReservations(
        longitude: Double, latitude: Double, radius: Double, radiusUnit: RadiusUnit,
    ): List<Reservation> {
        return async { getNearestAvailableReservations.invoke(longitude, latitude, radius, radiusUnit) }
    }

    suspend fun patchReservationMatch(input: PatchMatchInput, reservation: Reservation): Reservation {
        val userEmail = Email(input.value)
        return when (input.op) {
            OpType.remove -> async { leaveMatch(userEmail, reservation) }
            OpType.replace -> async { joinMatch(userEmail, reservation) }
        }
    }
}
