package com.octawizard.server.route

import com.octawizard.controller.reservation.ReservationController
import com.octawizard.domain.model.ClubReservationInfo
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.MatchResult
import com.octawizard.domain.model.MatchSet
import com.octawizard.domain.model.PaymentStatus
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.Reservation
import com.octawizard.domain.model.ReservationStatus
import com.octawizard.domain.model.User
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.server.AuthorizationException
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

private fun Application.testableModule(reservationController: ReservationController) {
    testableModuleWithDependencies(reservationController)
}

private fun Application.testableModuleWithDependencies(reservationController: ReservationController) {
    install(ContentNegotiation) {
        json(JsonSerde)
    }
    install(Authentication) {
        mockAuthConfig()
    }
    install(DefaultHeaders)
    install(CallLogging)
    install(Locations)
    install(StatusPages) {
        exception<AuthorizationException> {
            call.response.status(HttpStatusCode.Forbidden)
        }
        exception<IllegalStateException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.localizedMessage)
        }
        exception<kotlinx.serialization.SerializationException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.localizedMessage)
        }
    }
    routing {
        reservationRoutes(reservationController)
    }
}

private val user = User(
    Email("test@test.com"),
    "test-user",
    createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
)
private val field = Field(UUID.randomUUID(), "field", false, WallsMaterial.Bricks)

private fun createReservation(id: UUID): Reservation {
    return Reservation(
        id,
        Match(listOf(user), MatchResult(listOf(MatchSet(6, 4)))),
        ClubReservationInfo(UUID.randomUUID(), "club", field, GeoLocation(1.1, 2.1)),
        LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
        LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
        user,
        BigDecimal.ONE,
        ReservationStatus.Confirmed,
        PaymentStatus.Payed,
        LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    )
}

class ReservationRoutesTest {

    @Test
    fun `Server should not handle unknown routes`() {
        val reservationController = mockk<ReservationController>(relaxed = true)
        withTestApplication({ testableModule(reservationController) }) {
            with(handleRequest(HttpMethod.Get, "/not-valid")) {
                assertFalse(requestHandled)
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Nested
    inner class GetReservationRouteTest {
        @Test
        fun `Server should handle GET reservation##{id}`() {
            val reservationController = mockk<ReservationController>(relaxed = true)
            val reservationId = UUID.randomUUID()
            val reservation = createReservation(reservationId)

            // 200 - Ok
            coEvery { reservationController.getReservation(reservationId) } returns reservation
            withTestApplication({ testableModule(reservationController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/reservation/$reservationId",
                    UUID.randomUUID().toString())) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(reservation), response.content)
                }
            }

            // 400 - bad request
            withTestApplication({ testableModule(reservationController) }) {
                with(handleRequestWithJWT(HttpMethod.Get, "/reservation/not-valid-id", UUID.randomUUID().toString())) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("not a valid reservation id", response.content)
                }
            }

            // 404 - not found
            coEvery { reservationController.getReservation(reservationId) } returns null
            withTestApplication({ testableModule(reservationController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/reservation/$reservationId",
                    UUID.randomUUID().toString())) {
                    assertEquals(HttpStatusCode.NotFound, response.status())
                }
            }
        }

        @Test
        fun `Server should handle GET reservation##{id} - unauthenticated`() {
            val reservationController = mockk<ReservationController>(relaxed = true)
            val reservationId = UUID.randomUUID()

            withTestApplication({ testableModule(reservationController) }) {
                with(handleRequest(HttpMethod.Get, "/reservation/$reservationId")) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }

    }

    @Nested
    inner class GetNearestAvailableReservationsRouteTest {
        @Test
        fun `Server should handle GET reservations`() {
            val reservationController = mockk<ReservationController>(relaxed = true)
            val reservationId = UUID.randomUUID()
            val reservation = createReservation(reservationId)
            val reservations = listOf(reservation)
            val lon = 20.0
            val lat = 20.0
            val radius = 5.0
            val radiusUnit = RadiusUnit.Kilometers

            // 200 - Ok
            coEvery {
                reservationController.getNearestAvailableReservations(
                    lon,
                    lat,
                    radius,
                    radiusUnit,
                )
            } returns reservations
            withTestApplication({ testableModule(reservationController) }) {
                with(handleRequestWithJWT(
                    HttpMethod.Get,
                    "/reservations",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.LONGITUDE to lon.toString(),
                        QueryParams.LATITUDE to lat.toString(),
                        QueryParams.RADIUS to radius.toString(),
                        QueryParams.RADIUS_UNIT to radiusUnit.name,
                    ),
                )) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(reservations), response.content)
                }
            }

            // 400 - bad request - null longitude
            withTestApplication({ testableModule(reservationController) }) {
                with(handleRequestWithJWT(
                    HttpMethod.Get,
                    "/reservations",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.LATITUDE to lat.toString(),
                        QueryParams.RADIUS to radius.toString(),
                        QueryParams.RADIUS_UNIT to radiusUnit.name,
                    ),
                )) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals( "longitude cannot be null", response.content)
                }
            }

            // 400 - bad request - null latitude
            withTestApplication({ testableModule(reservationController) }) {
                with(handleRequestWithJWT(
                    HttpMethod.Get,
                    "/reservations",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.LONGITUDE to lon.toString(),
                        QueryParams.RADIUS to radius.toString(),
                        QueryParams.RADIUS_UNIT to radiusUnit.name,
                    ),
                )) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals( "latitude cannot be null", response.content)
                }
            }

            // 400 - bad request - null radius
            withTestApplication({ testableModule(reservationController) }) {
                with(handleRequestWithJWT(
                    HttpMethod.Get,
                    "/reservations",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.LONGITUDE to lon.toString(),
                        QueryParams.LATITUDE to lat.toString(),
                        QueryParams.RADIUS_UNIT to radiusUnit.name,
                    ),
                )) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals( "radius cannot be null", response.content)
                }
            }
        }

        @Test
        fun `Server should handle GET reservations - unauthenticated`() {
            val reservationController = mockk<ReservationController>(relaxed = true)
            withTestApplication({ testableModule(reservationController) }) {
                with(handleRequest(HttpMethod.Get, "/reservations")) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }

    }
}
