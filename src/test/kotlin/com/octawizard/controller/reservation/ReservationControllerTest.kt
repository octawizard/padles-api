package com.octawizard.controller.reservation

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
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReservationControllerTest {
    private val createReservation: CreateReservation = mockk()
    private val cancelReservation: CancelReservation = mockk()
    private val getReservation: GetReservation = mockk()
    private val getNearestAvailableReservations: GetNearestAvailableReservations = mockk()
    private val updateMatchResult: UpdateMatchResult = mockk()
    private val joinMatch: JoinMatch = mockk()
    private val leaveMatch: LeaveMatch = mockk()

    private val controller = ReservationController(
        createReservation,
        cancelReservation,
        getReservation,
        getNearestAvailableReservations,
        updateMatchResult,
        joinMatch,
        leaveMatch
    )

    @AfterEach
    fun `reset mocks`() {
        clearAllMocks()
    }

    @Test
    fun `ReservationController should call GetReservation to get a reservation`() {
        val reservation = mockk<Reservation>()
        every { getReservation(any()) } returns reservation

        val reservationId = UUID.randomUUID()
        assertEquals(reservation, runBlocking { controller.getReservation(reservationId) })
        verify { getReservation.invoke(reservationId) }
    }

    @Test
    fun `ReservationController should call CreateReservation to create a reservation`() {
        val reservation = mockk<Reservation>()
        val reservedBy = Email("user@padles.com")
        val clubId = UUID.randomUUID()
        val fieldId = UUID.randomUUID()
        val startTime = LocalDateTime.now()
        val endTime = startTime.plusHours(2)
        every { createReservation(any(), any(), any(), any(), any(), any(), any(), any()) } returns reservation

        assertEquals(reservation,
            runBlocking {
                controller.createReservation(reservedBy,
                    clubId,
                    fieldId,
                    startTime,
                    endTime,
                    null,
                    null,
                    null)
            })
        verify { createReservation(reservedBy, clubId, fieldId, startTime, endTime, null, null, null) }
    }

    @Test
    fun `ReservationController should call CancelReservation to cancel a reservation`() {
        val reservation = mockk<Reservation>()
        val reservationId = UUID.randomUUID()
        every { reservation.id } returns reservationId
        every { cancelReservation(any()) } returns reservation

        assertEquals(reservation, runBlocking { controller.cancelReservation(reservation) })
        verify { cancelReservation.invoke(reservationId) }
    }

    @Test
    fun `ReservationController should call UpdateReservationMatchResult to update the match result of the reservation`() {
        val reservation = mockk<Reservation>()
        val matchResult = mockk<MatchResult>()
        every { updateMatchResult(any(), any()) } returns reservation

        assertEquals(reservation, runBlocking { controller.updateReservationMatchResult(reservation, matchResult) })
        verify { updateMatchResult.invoke(reservation, matchResult) }
    }

    @Test
    fun `ReservationController should call GetNearestAvailableReservations to get the near available reservations`() {
        val reservations = listOf(mockk<Reservation>())
        every { getNearestAvailableReservations(any(), any(), any(), any()) } returns reservations

        assertEquals(reservations,
            runBlocking { controller.getNearestAvailableReservations(1.0, 1.0, 1.0, RadiusUnit.Kilometers) })
        verify { getNearestAvailableReservations.invoke(1.0, 1.0, 1.0, RadiusUnit.Kilometers) }
    }

    @Test
    fun `ReservationController should call PatchReservationMatch to add a player to a match`() {
        val reservation = mockk<Reservation>()
        every { joinMatch(any(), any()) } returns reservation
        val input = PatchMatchInput(op = OpType.add, value = "user@padles.com")

        assertEquals(reservation, runBlocking { controller.patchReservationMatch(input, reservation) })
        verify { joinMatch(Email(input.value), reservation) }
    }

    @Test
    fun `ReservationController should call PatchReservationMatch to remove a player from a match`() {
        val reservation = mockk<Reservation>()
        every { leaveMatch(any(), any()) } returns reservation
        val input = PatchMatchInput(op = OpType.remove, value = "user@padles.com")

        assertEquals(reservation, runBlocking { controller.patchReservationMatch(input, reservation) })
        verify { leaveMatch(Email(input.value), reservation) }
    }
}
