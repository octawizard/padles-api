package com.octawizard.server.route

import com.octawizard.controller.reservation.ReservationController
import com.octawizard.domain.model.MatchResult
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.Reservation
import com.octawizard.server.AuthorizationException
import com.octawizard.server.UserBasedAuthenticationConfig
import com.octawizard.server.input.CreateReservationInput
import com.octawizard.server.input.PatchMatchInput
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.routing.patch
import io.ktor.routing.put
import io.ktor.util.pipeline.*
import java.util.*

@KtorExperimentalLocationsAPI
@Location("/reservation/{reservationId}")
data class ReservationRoute(val reservationId: UUID)

@KtorExperimentalLocationsAPI
@Location("/reservation/{reservationId}/match")
data class MatchRoute(val reservationId: UUID)

@KtorExperimentalLocationsAPI
fun Routing.reservationRoutes(reservationController: ReservationController) {

    authenticate(UserBasedAuthenticationConfig) {

        // get reservation
        get<ReservationRoute> { reservationRoute ->
            val reservation = reservationController.getReservation(reservationRoute.reservationId)
                ?: reservationNotFound(reservationRoute.reservationId)
            call.respond(HttpStatusCode.OK, reservation)
        }

        // get nearest available reservation
        get("/reservations") {
            val longitude = call.request.queryParameters["lon"]?.toDoubleOrNull()
            val latitude = call.request.queryParameters["lat"]?.toDoubleOrNull()
            val radius = call.request.queryParameters["rad"]?.toDoubleOrNull()
            val radiusUnit = call.request.queryParameters["radUnit"]?.let { RadiusUnit.valueOf(it) }
                ?: RadiusUnit.Kilometers

            checkNotNull(longitude)
            checkNotNull(latitude)
            checkNotNull(radius)

            val reservations = reservationController.getNearestAvailableReservations(
                longitude, latitude, radius, radiusUnit
            )

            call.respond(HttpStatusCode.OK, reservations)
        }

        // create a reservation
        post("/reservation") {
            val input = call.receive<CreateReservationInput>()
            val reservation = reservationController.createReservation(
                input.reservedBy, input.clubId, input.fieldId, input.startTime, input.endTime,
                input.matchEmailPlayer2, input.matchEmailPlayer3, input.matchEmailPlayer4
            )
            call.respond(HttpStatusCode.Created, reservation)
        }

        // cancel reservation
        delete<ReservationRoute> { reservationRoute ->
            val reservation: Reservation = reservationController.getReservation(reservationRoute.reservationId)
                ?: reservationNotFound(reservationRoute.reservationId)
            authorizeReservationOwnerOnly(reservation)
            val canceledReservation = reservationController.cancelReservation(reservation)
            call.respond(HttpStatusCode.OK, canceledReservation)
        }

        // for the future, pay reservation
//            put<ReservationRoute> { reservationRoute ->
//                val reservation: Reservation = controller.getReservation(reservationRoute.reservationId)
//                        ?: reservationNotFound(reservationRoute.reservationId)
//                authorizeReservationOwnerOnly(reservation)
//                val input = call.receive<ReservationUpdateInput>()
//                controller.updateReservation(reservation, input.a, input.b, input.c)
//            }

        // update match result
        put<MatchRoute> { matchRoute ->
            val reservation = reservationController.getReservation(matchRoute.reservationId)
                ?: reservationNotFound(matchRoute.reservationId)
            authorizeReservationOwnerOnly(reservation)
            val matchResult = call.receive<MatchResult>()
            val updatedReservation = reservationController.updateReservationMatchResult(reservation, matchResult)
            call.respond(HttpStatusCode.OK, updatedReservation)
        }


        // join or leave a match
        patch<MatchRoute> { matchRoute ->
            val reservation = reservationController.getReservation(matchRoute.reservationId)
                ?: reservationNotFound(matchRoute.reservationId)
            val input = call.receive<PatchMatchInput>()
            val updateMatch = reservationController.patchReservationMatch(input, reservation)
            call.respond(HttpStatusCode.OK, updateMatch)
        }
    }

}

private fun reservationNotFound(reservationId: UUID): Reservation {
    throw NotFoundException("reservation $reservationId not found")
}

private fun PipelineContext<Unit, ApplicationCall>.authorizeReservationOwnerOnly(reservation: Reservation) {
    val principal = call.authentication.principal<JWTPrincipal>()
        ?: throw AuthorizationException("Missing principal")
    val tokenUserEmail = principal.payload.subject
    if (reservation.reservedBy.email.value != tokenUserEmail) {
        throw AuthorizationException("Cannot update/delete reservation - not reserved by $tokenUserEmail")
    }
}
