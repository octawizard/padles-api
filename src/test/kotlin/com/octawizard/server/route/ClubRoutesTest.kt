package com.octawizard.server.route

import com.octawizard.controller.club.ClubController
import com.octawizard.domain.model.Availability
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.EmptyAvailability
import com.octawizard.domain.model.Field
import com.octawizard.domain.model.FieldAvailability
import com.octawizard.domain.model.GeoLocation
import com.octawizard.domain.model.RadiusUnit
import com.octawizard.domain.model.TimeSlot
import com.octawizard.domain.model.WallsMaterial
import com.octawizard.server.AuthorizationException
import com.octawizard.server.input.AddClubFieldInput
import com.octawizard.server.input.ClubSearchCriteria
import com.octawizard.server.input.CreateClubInput
import com.octawizard.server.input.UpdateClubAddressInput
import com.octawizard.server.input.UpdateClubAvailabilityInput
import com.octawizard.server.input.UpdateClubAvgPriceInput
import com.octawizard.server.input.UpdateClubContactsInput
import com.octawizard.server.input.UpdateClubFieldInput
import com.octawizard.server.input.UpdateClubNameInput
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
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

private fun Application.testableModule(clubController: ClubController) {
    testableModuleWithDependencies(clubController)
}

@KtorExperimentalLocationsAPI
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
            Availability(
                mapOf(
                    LocalDate.now() to listOf(FieldAvailability(timeslot, field, BigDecimal.TEN)),
                    LocalDate.now().plusDays(1) to emptyList()
                )
            ),
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
                with(
                    handleRequestWithJWT(
                        HttpMethod.Post,
                        "/club",
                        "anonymous",
                        body = JsonSerde.encodeToString(input)
                    )
                ) {
                    assertEquals(HttpStatusCode.Created, response.status())
                    assertEquals(JsonSerde.encodeToString(club), response.content)
                }
            }

            // 400 - bad input
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Post,
                        "/club",
                        "anonymous",
                        body = JsonSerde.encodeToString(UpdateClubNameInput("wrong"))
                    )
                ) {
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
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
                        "/clubs",
                        UUID.randomUUID().toString(),
                        queryParams = mapOf(
                            QueryParams.CRITERIA to ClubSearchCriteria.ByName.name,
                            QueryParams.NAME to club.name,
                        )
                    )
                ) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(clubs), response.content)
                }
            }

            // 400 - Bad Request - null name
            coEvery { clubController.searchClubsByName(club.name) } returns clubs
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
                        "/clubs",
                        UUID.randomUUID().toString(),
                        queryParams = mapOf(QueryParams.CRITERIA to ClubSearchCriteria.ByName.name)
                    )
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param name cannot be null or empty", response.content)
                }
            }

            // 400 - Bad Request - empty name
            coEvery { clubController.searchClubsByName(club.name) } returns clubs
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
                        "/clubs",
                        UUID.randomUUID().toString(),
                        queryParams = mapOf(
                            QueryParams.CRITERIA to ClubSearchCriteria.ByName.name,
                            QueryParams.NAME to "",
                        )
                    )
                ) {
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
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
                        "/clubs",
                        UUID.randomUUID().toString(),
                        queryParams = mapOf(
                            QueryParams.CRITERIA to ClubSearchCriteria.ByDistance.name,
                            QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                            QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                            QueryParams.RADIUS to radius.toString(),
                            QueryParams.RADIUS_UNIT to RadiusUnit.Miles.name,
                        )
                    )
                ) {
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
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
                        "/clubs",
                        UUID.randomUUID().toString(),
                        queryParams = mapOf(
                            QueryParams.CRITERIA to ClubSearchCriteria.ByDistance.name,
                            QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                            QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                            QueryParams.RADIUS to radius.toString(),
                        )
                    )
                ) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(clubs), response.content)
                }
            }

            // 400 - Bad Request - null longitude
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
                        "/clubs",
                        UUID.randomUUID().toString(),
                        queryParams = mapOf(
                            QueryParams.CRITERIA to ClubSearchCriteria.ByDistance.name,
                            QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                            QueryParams.RADIUS to radius.toString(),
                        )
                    )
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param longitude cannot be null", response.content)
                }
            }

            // 400 - Bad Request - null latitude
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
                        "/clubs",
                        UUID.randomUUID().toString(),
                        queryParams = mapOf(
                            QueryParams.CRITERIA to ClubSearchCriteria.ByDistance.name,
                            QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                            QueryParams.RADIUS to radius.toString(),
                        )
                    )
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param latitude cannot be null", response.content)
                }
            }

            // 400 - Bad Request - null radius
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
                        "/clubs",
                        UUID.randomUUID().toString(),
                        queryParams = mapOf(
                            QueryParams.CRITERIA to ClubSearchCriteria.ByDistance.name,
                            QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                            QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                        )
                    )
                ) {
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
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
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
                    )
                ) {
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
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
                        "/clubs",
                        UUID.randomUUID().toString(),
                        queryParams = mapOf(
                            QueryParams.CRITERIA to ClubSearchCriteria.ByDistanceAndDayAvailability.name,
                            QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                            QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                            QueryParams.RADIUS to radius.toString(),
                            QueryParams.DAY to day.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        )
                    )
                ) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(clubs), response.content)
                }
            }

            // 400 - bad request - longitude null
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
                        "/clubs",
                        UUID.randomUUID().toString(),
                        queryParams = mapOf(
                            QueryParams.CRITERIA to ClubSearchCriteria.ByDistanceAndDayAvailability.name,
                            QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                            QueryParams.RADIUS to radius.toString(),
                            QueryParams.DAY to day.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        )
                    )
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param longitude cannot be null", response.content)
                }
            }

            // 400 - bad request - latitude null
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
                        "/clubs",
                        UUID.randomUUID().toString(),
                        queryParams = mapOf(
                            QueryParams.CRITERIA to ClubSearchCriteria.ByDistanceAndDayAvailability.name,
                            QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                            QueryParams.RADIUS to radius.toString(),
                            QueryParams.DAY to day.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        )
                    )
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param latitude cannot be null", response.content)
                }
            }

            // 400 - bad request - day null
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
                        "/clubs",
                        UUID.randomUUID().toString(),
                        queryParams = mapOf(
                            QueryParams.CRITERIA to ClubSearchCriteria.ByDistanceAndDayAvailability.name,
                            QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                            QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                            QueryParams.RADIUS to radius.toString(),
                        )
                    )
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                    assertEquals("query param day cannot be null", response.content)
                }
            }

            // 400 - bad request - radius null
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Get,
                        "/clubs",
                        UUID.randomUUID().toString(),
                        queryParams = mapOf(
                            QueryParams.CRITERIA to ClubSearchCriteria.ByDistanceAndDayAvailability.name,
                            QueryParams.LONGITUDE to club.geoLocation.longitude.toString(),
                            QueryParams.LATITUDE to club.geoLocation.latitude.toString(),
                            QueryParams.DAY to day.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        )
                    )
                ) {
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
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            val clubName = "new club name"
            val updatedClub = club.copy(name = clubName)

            // 200 - Ok
            coEvery { clubController.updateClubName(club, clubName) } returns updatedClub
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/name",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(UpdateClubNameInput(clubName)),
                    )
                ) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(updatedClub), response.content)
                }
            }

            // 400 - Bad Request
            coEvery { clubController.updateClubName(club, clubName) } returns updatedClub
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/name",
                        club.id.toString(),
                        body = """{ "wrong" : "body" }""",
                    )
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            }

            // 404 - Not Found - club not found
            coEvery { clubController.getClub(club.id) } returns null
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/name",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(UpdateClubNameInput(clubName)),
                    )
                ) {
                    assertEquals(HttpStatusCode.NotFound, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT club - update name - unauthorized`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            val clubName = "new club name"
            // 403
            coEvery { clubController.updateClubName(club, clubName) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/name",
                        UUID.randomUUID().toString(),
                        body = JsonSerde.encodeToString(UpdateClubNameInput(clubName)),
                    )
                ) {
                    assertEquals(HttpStatusCode.Forbidden, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT club - update name - unauthenticated`() {
            // 401
            withTestApplication({ testableModule(mockk(relaxed = true)) }) {
                with(handleRequest(HttpMethod.Put, "/club/${UUID.randomUUID()}/name")) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }

    @Nested
    inner class UpdateClubAddressRoute {

        @Test
        fun `Server should handle PUT club - update address`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            val address = "new club address"
            val location = GeoLocation(30.0, 30.0)
            val updatedClub = club.copy(address = address, geoLocation = location)

            // 200 - Ok
            coEvery { clubController.updateClubAddress(club, address, location) } returns updatedClub
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/address",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(UpdateClubAddressInput(address, location)),
                    )
                ) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(updatedClub), response.content)
                }
            }

            // 400 - Bad Request
            coEvery { clubController.updateClubAddress(club, address, location) } returns updatedClub
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/address",
                        club.id.toString(),
                        body = """{ "wrong" : "body" }""",
                    )
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            }

            // 404 - Not Found - club not found
            coEvery { clubController.getClub(club.id) } returns null
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/address",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(UpdateClubAddressInput(address, location)),
                    )
                ) {
                    assertEquals(HttpStatusCode.NotFound, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT club - update address - unauthorized`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            // 403
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/address",
                        UUID.randomUUID().toString(),
                        body = JsonSerde.encodeToString(UpdateClubAddressInput(club.address, club.geoLocation)),
                    )
                ) {
                    assertEquals(HttpStatusCode.Forbidden, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT club - update address - unauthenticated`() {
            // 401
            withTestApplication({ testableModule(mockk(relaxed = true)) }) {
                with(handleRequest(HttpMethod.Put, "/club/${UUID.randomUUID()}/address")) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }

    @Nested
    inner class UpdateClubContactsRoute {

        @Test
        fun `Server should handle PUT club - update contacts`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            val contacts = Contacts("new phone", Email("email@test.com"))
            val updatedClub = club.copy(contacts = contacts)

            // 200 - Ok
            coEvery { clubController.updateClubContacts(club, contacts) } returns updatedClub
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/contacts",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(UpdateClubContactsInput(contacts.phone, contacts.email)),
                    )
                ) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(updatedClub), response.content)
                }
            }

            // 400 - Bad Request
            coEvery { clubController.updateClubContacts(club, contacts) } returns updatedClub
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/contacts",
                        club.id.toString(),
                        body = """{ "wrong" : "body" }""",
                    )
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            }

            // 404 - Not Found - club not found
            coEvery { clubController.getClub(club.id) } returns null
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/contacts",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(UpdateClubContactsInput(contacts.phone, contacts.email)),
                    )
                ) {
                    assertEquals(HttpStatusCode.NotFound, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT club - update contacts - unauthorized`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            val contacts = Contacts("new phone", Email("email@test.com"))
            // 403
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/contacts",
                        UUID.randomUUID().toString(),
                        body = JsonSerde.encodeToString(UpdateClubContactsInput(contacts.phone, contacts.email)),
                    )
                ) {
                    assertEquals(HttpStatusCode.Forbidden, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT club - update contacts - unauthenticated`() {
            // 401
            withTestApplication({ testableModule(mockk(relaxed = true)) }) {
                with(handleRequest(HttpMethod.Put, "/club/${UUID.randomUUID()}/contacts")) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }

    @Nested
    inner class UpdateClubAvgPriceRoute {

        @Test
        fun `Server should handle PUT club - update avg price`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            val avgPrice = BigDecimal("5")
            val updatedClub = club.copy(avgPrice = avgPrice)

            // 200 - Ok
            coEvery { clubController.updateClubAvgPrice(club, avgPrice) } returns updatedClub
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/avg_price",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(UpdateClubAvgPriceInput(avgPrice)),
                    )
                ) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(updatedClub), response.content)
                }
            }

            // 400 - Bad Request
            coEvery { clubController.updateClubAvgPrice(club, avgPrice) } returns updatedClub
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/avg_price",
                        club.id.toString(),
                        body = """{ "wrong" : "body" }""",
                    )
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            }

            // 404 - Not Found - club not found
            coEvery { clubController.getClub(club.id) } returns null
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/avg_price",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(UpdateClubAvgPriceInput(avgPrice)),
                    )
                ) {
                    assertEquals(HttpStatusCode.NotFound, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT club - update avg price - unauthorized`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            // 403
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/avg_price",
                        UUID.randomUUID().toString(),
                        body = JsonSerde.encodeToString(UpdateClubAvgPriceInput(BigDecimal.ONE)),
                    )
                ) {
                    assertEquals(HttpStatusCode.Forbidden, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT club - update avg price - unauthenticated`() {
            // 401
            withTestApplication({ testableModule(mockk(relaxed = true)) }) {
                with(handleRequest(HttpMethod.Put, "/club/${UUID.randomUUID()}/avg_price")) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }

    @Nested
    inner class UpdateClubAddFieldRoute {

        @Test
        fun `Server should handle POST club##fields - add field to club`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            val field = field.copy(UUID.randomUUID())
            val updatedClub = club.copy(fields = (club.fields + field))

            // 200 - Ok
            coEvery {
                clubController.addToClubFields(
                    club,
                    field.name,
                    field.isIndoor,
                    field.hasSand,
                    field.wallsMaterial,
                )
            } returns updatedClub
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Post,
                        "/club/${club.id}/fields",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(
                            AddClubFieldInput(
                                field.name,
                                field.isIndoor,
                                field.wallsMaterial,
                                field.hasSand,
                            )
                        ),
                    )
                ) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(updatedClub), response.content)
                }
            }

            // 400 - Bad Request
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Post,
                        "/club/${club.id}/fields",
                        club.id.toString(),
                        body = """{ "wrong" : "body" }""",
                    )
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            }

            // 404 - Not Found - club not found
            coEvery { clubController.getClub(club.id) } returns null
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Post,
                        "/club/${club.id}/fields",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(
                            AddClubFieldInput(
                                field.name,
                                field.isIndoor,
                                field.wallsMaterial,
                                field.hasSand,
                            )
                        ),
                    )
                ) {
                    assertEquals(HttpStatusCode.NotFound, response.status())
                }
            }
        }

        @Test
        fun `Server should handle POST club##fields - add field to club - unauthorized`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            // 403
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Post,
                        "/club/${club.id}/fields",
                        UUID.randomUUID().toString(),
                        body = JsonSerde.encodeToString(
                            AddClubFieldInput(
                                field.name,
                                field.isIndoor,
                                field.wallsMaterial,
                                field.hasSand,
                            )
                        ),
                    )
                ) {
                    assertEquals(HttpStatusCode.Forbidden, response.status())
                }
            }
        }

        @Test
        fun `Server should handle POST club##fields - add field to club - unauthenticated`() {
            // 401
            withTestApplication({ testableModule(mockk(relaxed = true)) }) {
                with(handleRequest(HttpMethod.Post, "/club/${UUID.randomUUID()}/fields")) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }

    @Nested
    inner class UpdateClubUpdateFieldRoute {
        @Test
        fun `Server should handle PUT club##field - update club field`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            val field = field.copy(UUID.randomUUID())
            val updatedClub = club.copy(fields = (club.fields + field))

            // 200 - Ok
            coEvery {
                clubController.updateClubField(
                    club,
                    field.id,
                    field.name,
                    field.isIndoor,
                    field.hasSand,
                    field.wallsMaterial,
                )
            } returns updatedClub
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/field/${field.id}",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(
                            UpdateClubFieldInput(
                                field.name,
                                field.isIndoor,
                                field.wallsMaterial,
                                field.hasSand,
                            )
                        ),
                    )
                ) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(updatedClub), response.content)
                }
            }

            // 400 - Bad Request
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/field/${field.id}",
                        club.id.toString(),
                        body = """{ "wrong" : "body" }""",
                    )
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            }

            // 404 - Not Found - club not found
            coEvery { clubController.getClub(club.id) } returns null
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/field/${field.id}",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(
                            UpdateClubFieldInput(
                                field.name,
                                field.isIndoor,
                                field.wallsMaterial,
                                field.hasSand,
                            )
                        ),
                    )
                ) {
                    assertEquals(HttpStatusCode.NotFound, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT club##field - update club field - unauthorized`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            // 403
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/field/${field.id}",
                        UUID.randomUUID().toString(),
                        body = JsonSerde.encodeToString(
                            UpdateClubFieldInput(
                                field.name,
                                field.isIndoor,
                                field.wallsMaterial,
                                field.hasSand,
                            )
                        ),
                    )
                ) {
                    assertEquals(HttpStatusCode.Forbidden, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT club##field - update club field - unauthenticated`() {
            // 401
            withTestApplication({ testableModule(mockk(relaxed = true)) }) {
                with(handleRequest(HttpMethod.Put, "/club/${UUID.randomUUID()}/field/${field.id}")) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }

    @Nested
    inner class UpdateAvailabilityRoute {
        @Test
        fun `Server should handle PUT club - update availability`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            val updatedClub = club.copy(availability = EmptyAvailability)

            // 200 - Ok
            coEvery { clubController.updateClubAvailability(club, EmptyAvailability) } returns updatedClub
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/availability",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(UpdateClubAvailabilityInput(EmptyAvailability)),
                    )
                ) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertEquals(JsonSerde.encodeToString(updatedClub), response.content)
                }
            }

            // 400 - Bad Request
            coEvery { clubController.getClub(club.id) } returns club
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/availability",
                        club.id.toString(),
                        body = """{ "wrong" : "body" }""",
                    )
                ) {
                    assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            }

            // 404 - Not Found - club not found
            coEvery { clubController.getClub(club.id) } returns null
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/availability",
                        club.id.toString(),
                        body = JsonSerde.encodeToString(UpdateClubAvailabilityInput(EmptyAvailability)),
                    )
                ) {
                    assertEquals(HttpStatusCode.NotFound, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT club - update club availability - unauthorized`() {
            val clubController = mockk<ClubController>(relaxed = true)
            val club = getClub(UUID.randomUUID())
            // 403
            withTestApplication({ testableModule(clubController) }) {
                with(
                    handleRequestWithJWT(
                        HttpMethod.Put,
                        "/club/${club.id}/availability",
                        UUID.randomUUID().toString(),
                        body = JsonSerde.encodeToString(UpdateClubAvailabilityInput(EmptyAvailability)),
                    )
                ) {
                    assertEquals(HttpStatusCode.Forbidden, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT club - update club availability - unauthenticated`() {
            // 401
            withTestApplication({ testableModule(mockk(relaxed = true)) }) {
                with(handleRequest(HttpMethod.Put, "/club/${UUID.randomUUID()}/availability")) {
                    assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }
}
