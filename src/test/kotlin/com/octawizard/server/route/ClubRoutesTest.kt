package com.octawizard.server.route

import com.octawizard.controller.club.ClubController
import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.FieldAvailability
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.TimeSlot
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.server.AuthorizationException
import com.octawizard.server.input.ClubSearchCriteria
import com.octawizard.server.input.CreateClubInput
import com.octawizard.server.input.UpdateClubNameInput
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun Application.testableModule(clubController: ClubController) {
    testableModuleWithDependencies(clubController)
}

private fun Application.testableModuleWithDependencies(clubController: ClubController) {
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
        clubRoutes(clubController)
    }
}

class ClubRoutesTest {

    private val field = Field(UUID.randomUUID(), "field", false, WallsMaterial.Bricks)
    private val now = LocalDateTime.now()
    private val timeslot = TimeSlot(now.plusHours(1), now.plusHours(2))
    private fun getClub(id: UUID): Club =
        Club(
            id,
            "club name",
            "club address",
            GeoLocation(1.1, 1.1),
            setOf(field),
            Availability(mapOf(
                LocalDate.now() to listOf(FieldAvailability(timeslot, field, BigDecimal.TEN)),
                LocalDate.now().plusDays(1) to emptyList()
            )),
            BigDecimal.TEN,
            Contacts("21451", Email("club@test.com")),
        )

    @Test
    fun `Server should not handle unknown routes`() {
        val clubController = mockk<ClubController>(relaxed = true)
        withTestApplication({ testableModule(clubController) }) {
            with(handleRequest(HttpMethod.Get, "/not-valid")) {
                assertFalse(requestHandled)
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Nested
    inner class GetClubRouteTest {
        @Test
        fun `Server should handle GET club##{id}`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val clubId = UUID.randomUUID()
            val club = getClub(clubId)

            // 200 - Ok
            coEvery { clubController.getClub(clubId) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get, "/club/$clubId", UUID.randomUUID().toString())) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(club), response.content)
                }
            }

            // 404 - not found
            coEvery { clubController.getClub(clubId) } returns null
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get, "/club/$clubId", UUID.randomUUID().toString())) {
                    assertEquals(HttpStatusCode.NotFound, response.status())
                }
            }
        }

        @Test
        fun `Server should handle GET club##{id} - unauthenticated`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val clubId = UUID.randomUUID()

            withTestApplication({ testableModule(clubController) }) {
                with(handleRequest(HttpMethod.Get, "/club/$clubId")) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }

    }

    @Nested
    inner class PostClubRouteTest {
        @Test
        fun `Server should handle POST club`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val clubId = UUID.randomUUID()
            val club = getClub(clubId)

            coEvery {
                clubController.createClub(
                    club.name,
                    club.address,
                    club.geoLocation,
                    club.avgPrice,
                    club.contacts,
                    club.fields,
                    club.availability,
                )
            } returns club

            val input =
                CreateClubInput(
                    club.name,
                    club.address,
                    club.geoLocation,
                    club.avgPrice,
                    club.contacts,
                    club.fields,
                    club.availability,
                )

            // 201 - Created
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Post, "/club", "anonymous", body = JsonSerde.encodeToString(input))) {
                    assertEquals(HttpStatusCode.Created, response.status())
                    assertEquals(JsonSerde.encodeToString(club), response.content)
                }
            }

            // 400 - bad input
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Post,
                    "/club",
                    "anonymous",
                    body = JsonSerde.encodeToString(UpdateClubNameInput("wrong"))
                )) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            }
        }

        @Test
        fun `Server should handle POST club - unauthenticated`() {
            val clubController = mockk<ClubController>(relaxed = true)
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequest(HttpMethod.Post, "/club")) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }

    @Nested
    inner class GetClubsRouteTest {
        @Test
        fun `Server should handle GET clubs - ByName criteria`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            val clubs = listOf(club)

            // 200 - Ok
            coEvery { clubController.searchClubsByName(club.name) } returns clubs
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.CRITERIA to ClubSearchCriteria.ByName.name,
                        QueryParams.NAME to club.name,
                    )
                )) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(clubs), response.content)
                }
            }

            // 400 - Bad Request - null name
            coEvery { clubController.searchClubsByName(club.name) } returns clubs
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(QueryParams.CRITERIA to ClubSearchCriteria.ByName.name)
                )) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param name cannot be null or empty", response.content)
                }
            }

            // 400 - Bad Request - empty name
            coEvery { clubController.searchClubsByName(club.name) } returns clubs
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.CRITERIA to ClubSearchCriteria.ByName.name,
                        QueryParams.NAME to "",
                    )
                )) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param name cannot be null or empty", response.content)
                }
            }
        }

        @Test
        fun `Server should handle GET clubs - ByDistance criteria`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            val clubs = listOf(club)
            val radius = 1.0

            // 200 - Ok
            coEvery {
                clubController.getNearestClubs(
                    club.geoLocation.longitude,
                    club.geoLocation.latitude,
                    radius,
                    RadiusUnit.Miles,
                )
            } returns clubs
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.CRITERIA to ClubSearchCriteria.ByDistance.name,
                        QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                        QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                        QueryParams.RADIUS to radius.toString(),
                        QueryParams.RADIUS_UNIT to RadiusUnit.Miles.name,
                    )
                )) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(clubs), response.content)
                }
            }

            // 200 - Bad Request - null radius unit
            coEvery {
                clubController.getNearestClubs(
                    club.geoLocation.longitude,
                    club.geoLocation.latitude,
                    radius,
                    RadiusUnit.Kilometers,
                )
            } returns clubs
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.CRITERIA to ClubSearchCriteria.ByDistance.name,
                        QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                        QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                        QueryParams.RADIUS to radius.toString(),
                    )
                )) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(clubs), response.content)
                }
            }

            // 400 - Bad Request - null longitude
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.CRITERIA to ClubSearchCriteria.ByDistance.name,
                        QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                        QueryParams.RADIUS to radius.toString(),
                    )
                )) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param longitude cannot be null", response.content)
                }
            }

            // 400 - Bad Request - null latitude
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.CRITERIA to ClubSearchCriteria.ByDistance.name,
                        QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                        QueryParams.RADIUS to radius.toString(),
                    )
                )) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param latitude cannot be null", response.content)
                }
            }

            // 400 - Bad Request - null radius
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.CRITERIA to ClubSearchCriteria.ByDistance.name,
                        QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                        QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                    )
                )) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param radius cannot be null", response.content)
                }
            }
        }

        @Test
        fun `Server should handle GET clubs - ByDistanceAndDayAvailability criteria`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            val clubs = listOf(club)
            val radius = 1.0
            val day = LocalDate.now()

            // 200 - Ok
            coEvery {
                clubController.getAvailableNearestClubs(
                    club.geoLocation.longitude,
                    club.geoLocation.latitude,
                    radius,
                    RadiusUnit.Miles,
                    day,
                )
            } returns clubs
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.CRITERIA to ClubSearchCriteria.ByDistanceAndDayAvailability.name,
                        QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                        QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                        QueryParams.RADIUS to radius.toString(),
                        QueryParams.RADIUS_UNIT to RadiusUnit.Miles.name,
                        QueryParams.DAY to day.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    )
                )) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(clubs), response.content)
                }
            }

            // 200 - radius unit null
            coEvery {
                clubController.getAvailableNearestClubs(
                    club.geoLocation.longitude,
                    club.geoLocation.latitude,
                    radius,
                    RadiusUnit.Kilometers,
                    day,
                )
            } returns clubs
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.CRITERIA to ClubSearchCriteria.ByDistanceAndDayAvailability.name,
                        QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                        QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                        QueryParams.RADIUS to radius.toString(),
                        QueryParams.DAY to day.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    )
                )) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(clubs), response.content)
                }
            }

            // 400 - bad request - longitude null
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.CRITERIA to ClubSearchCriteria.ByDistanceAndDayAvailability.name,
                        QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                        QueryParams.RADIUS to radius.toString(),
                        QueryParams.DAY to day.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    )
                )) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param longitude cannot be null", response.content)
                }
            }

            // 400 - bad request - latitude null
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.CRITERIA to ClubSearchCriteria.ByDistanceAndDayAvailability.name,
                        QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                        QueryParams.RADIUS to radius.toString(),
                        QueryParams.DAY to day.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    )
                )) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param latitude cannot be null", response.content)
                }
            }

            // 400 - bad request - day null
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.CRITERIA to ClubSearchCriteria.ByDistanceAndDayAvailability.name,
                        QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                        QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                        QueryParams.RADIUS to radius.toString(),
                    )
                )) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param day cannot be null", response.content)
                }
            }

            // 400 - bad request - radius null
            withTestApplication({ testableModule(clubController) }) {
                with(handleRequestWithJWT(HttpMethod.Get,
                    "/clubs",
                    UUID.randomUUID().toString(),
                    queryParams = mapOf(
                        QueryParams.CRITERIA to ClubSearchCriteria.ByDistanceAndDayAvailability.name,
                        QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                        QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                        QueryParams.DAY to day.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    )
                )) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param radius cannot be null", response.content)
                }
            }
        }

        @Test
        fun `Server should handle GET clubs - unauthenticated`() {
            withTestApplication({ testableModule(mockk(relaxed = true)) }) {
                with(handleRequest(HttpMethod.Get, "/clubs")) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }

    @Nested
    inner class UpdateClubNameRoute {

        @Test
        fun `Server should handle PUT club - update name`() {

        }

        @Test
        fun `Server should handle PUT club - update name - unauthorized`() {

        }

        @Test
        fun `Server should handle PUT club - update name - unauthenticated`() {

        }
    }

}

