package com.octawizard.server.route

import com.google.gson.Gson
import com.octawizard.controller.club.ClubController
import com.octawizard.domain.model.Club
import com.octawizard.domain.model.Contacts
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.EmptyAvailability
import com.octawizard.domain.model.GeoLocation
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

class ClubRoutesTest {

    private val gson = Gson()
    private fun getClub(id: UUID): Club =
        Club(
            id,
            "club name",
            "club address",
            GeoLocation(1.1, 1.1),
            emptySet(),
            EmptyAvailability,
            BigDecimal.TEN,
            Contacts("21451", Email("club@test.com")),
        )

    private fun Application.testableModule(clubController: ClubController) {
        testableModuleWithDependencies(clubController)
    }

    private fun Application.testableModuleWithDependencies(clubController: ClubController) {
        install(ContentNegotiation) {
            gson { }
        }
        install(Authentication) {
            mockAuthConfig()
        }
        install(DefaultHeaders)
        install(CallLogging)
        install(Locations)
        routing {
            clubRoutes(clubController)
        }
    }

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

    @Test
    fun `Server should handle GET club##{id}`() {
        val clubController = mockk<ClubController>(relaxed = true)
        val clubId = UUID.randomUUID()
        val club = getClub(clubId)

        coEvery { clubController.getClub(clubId) } returns club
        withTestApplication({ testableModule(clubController) }) {
            with(handleRequestWithJWT(HttpMethod.Get, "/club/$clubId", UUID.randomUUID().toString())) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(gson.toJson(club), response.content)
            }
        }

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
