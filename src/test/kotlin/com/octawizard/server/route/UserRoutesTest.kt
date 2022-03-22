package com.octawizard.server.route

import com.octawizard.controller.user.UserController
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Gender
import com.octawizard.domain.model.User
import com.octawizard.server.AuthorizationException
import com.octawizard.server.UserEmailBasedAuthorization
import com.octawizard.server.input.CreateUserInput
import com.octawizard.server.input.UserUpdateInput
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.testing.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

private fun Application.testableModule(userController: UserController) {
    testableModuleWithDependencies(userController)
}

@KtorExperimentalLocationsAPI
private fun Application.testableModuleWithDependencies(userController: UserController) {
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
    install(UserEmailBasedAuthorization)
    routing {
        userRoutes(userController)
    }
}

class UserRoutesTest {

    @Test
    fun `Server should not handle unknown routes`() {
        val userController = mockk<UserController>(relaxed = true)
        withTestApplication({ testableModule(userController) }) {
            with(handleRequest(HttpMethod.Get, "/not-valid")) {
                Assertions.assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Nested
    inner class GetUserRouteTest {

        @Test
        fun `Server should handle GET user##{email}`() {
            val userController = mockk<UserController>(relaxed = true)
            val email = Email("user@padles.com")
            val user = User(email, "user")

            // 200 - Ok
            coEvery { userController.getUser(email.value) } returns user
            withTestApplication({ testableModule(userController) }) {
                with(handleRequestWithJWT(HttpMethod.Get, "/user/${email.value}", UUID.randomUUID().toString())) {
                    Assertions.assertEquals(HttpStatusCode.OK, response.status())
                    Assertions.assertEquals(JsonSerde.encodeToString(user), response.content)
                }
            }

            // 400 - Bad Request
            coEvery { userController.getUser(email.value) } returns user
            withTestApplication({ testableModule(userController) }) {
                with(handleRequestWithJWT(HttpMethod.Get, "/user/not-an-email", UUID.randomUUID().toString())) {
                    Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            }

            // 404 - not found
            coEvery { userController.getUser(email.value) } returns null
            withTestApplication({ testableModule(userController) }) {
                with(handleRequestWithJWT(HttpMethod.Get, "/user/${email.value}", UUID.randomUUID().toString())) {
                    Assertions.assertEquals(HttpStatusCode.NotFound, response.status())
                }
            }
        }

        @Test
        fun `Server should handle GET user##{email} - unauthenticated`() {
            val userController = mockk<UserController>(relaxed = true)
            val email = Email("user@padles.com")

            withTestApplication({ testableModule(userController) }) {
                with(handleRequest(HttpMethod.Get, "/user/${email.value}")) {
                    Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }

    @Nested
    inner class CreateUserRouteTest {

        @Test
        fun `Server should handle POST user`() {
            val userController = mockk<UserController>(relaxed = true)
            val email = Email("user@padles.com")
            val createUserInput = CreateUserInput(Email("user@padles.com"), "name", Gender.Male, null)
            val user = createUserInput.toUser()

            // 201
            coEvery { userController.createUser(any()) } returns user
            withTestApplication({ testableModule(userController) }) {
                with(handleRequestWithJWT(
                    HttpMethod.Post, "/user", email.value, body = JsonSerde.encodeToString(createUserInput),
                )) {
                    Assertions.assertEquals(HttpStatusCode.Created, response.status())
                    Assertions.assertEquals(JsonSerde.encodeToString(user), response.content)
                }
            }

            // 400
            withTestApplication({ testableModule(userController) }) {
                with(handleRequestWithJWT(
                    HttpMethod.Post, "/user", email.value, body = """{ "x" : "y" }""",
                )) {
                    Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            }
        }

        @Test
        fun `Server should handle POST user - unauthenticated`() {
            withTestApplication({ testableModule(mockk(relaxed = true)) }) {
                with(handleRequest(HttpMethod.Post, "/user")) {
                    Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }

    @Nested
    inner class UpdateUserRouteTest {

        @Test
        fun `Server should handle PUT user##{email}`() {
            val userController = mockk<UserController>(relaxed = true)
            val email = Email("user@padles.com")
            val user = User(email, "user", phone = "phone")

            // 200 - Ok
            val input = UserUpdateInput(user.name, user.gender, user.phone)
            coEvery { userController.updateUser(any()) } returns user
            withTestApplication({ testableModule(userController) }) {
                with(handleRequestWithJWT(
                    HttpMethod.Put,
                    "/user/${email.value}",
                    email.value,
                    body = JsonSerde.encodeToString(input),
                )) {
                    Assertions.assertEquals(HttpStatusCode.OK, response.status())
                    Assertions.assertEquals(JsonSerde.encodeToString(user), response.content)
                }
            }

            // 400 - Bad Request
            withTestApplication({ testableModule(userController) }) {
                with(handleRequestWithJWT(
                    HttpMethod.Put,
                    "/user/${email.value}",
                    email.value,
                    body = """{"x": "y" }""",
                )) {
                    Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
                }
            }

            // 404 - not found
            coEvery { userController.updateUser(any()) } returns null
            withTestApplication({ testableModule(userController) }) {
                with(handleRequestWithJWT(
                    HttpMethod.Put,
                    "/user/${email.value}",
                    email.value,
                    body = JsonSerde.encodeToString(input),
                )) {
                    Assertions.assertEquals(HttpStatusCode.NotFound, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT user##{email} - unauthorized`() {
            val userController = mockk<UserController>(relaxed = true)
            val email = Email("user@padles.com")

            withTestApplication({ testableModule(userController) }) {
                with(handleRequestWithJWT(HttpMethod.Put, "/user/${email.value}", "another-email@padles.com")) {
                    Assertions.assertEquals(HttpStatusCode.Forbidden, response.status())
                }
            }
        }

        @Test
        fun `Server should handle PUT user##{email} - unauthenticated`() {
            val userController = mockk<UserController>(relaxed = true)
            val email = Email("user@padles.com")

            withTestApplication({ testableModule(userController) }) {
                with(handleRequest(HttpMethod.Put, "/user/${email.value}")) {
                    Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }

    @Nested
    inner class DeleteUserRouteTest {

        @Test
        fun `Server should handle DELETE user##{email}`() {
            val userController = mockk<UserController>(relaxed = true)
            val email = Email("user@padles.com")

            // 200 - Ok
            withTestApplication({ testableModule(userController) }) {
                with(handleRequestWithJWT(
                    HttpMethod.Delete,
                    "/user/${email.value}",
                    email.value,
                )) {
                    Assertions.assertEquals(HttpStatusCode.NoContent, response.status())
                    coVerify { userController.deleteUser(email) }
                }
            }

            // 400 - Bad Request
            clearMocks(userController)
            withTestApplication({ testableModule(userController) }) {
                with(handleRequestWithJWT(
                    HttpMethod.Delete,
                    "/user/not-valid-email",
                    email.value,
                )) {
                    Assertions.assertEquals(HttpStatusCode.BadRequest, response.status())
                    coVerify(exactly = 0) { userController.deleteUser(email) }
                }
            }
        }

        @Test
        fun `Server should handle DELETE user##{email} - unauthorized`() {
            val userController = mockk<UserController>(relaxed = true)
            val email = Email("user@padles.com")

            withTestApplication({ testableModule(userController) }) {
                with(handleRequestWithJWT(HttpMethod.Delete, "/user/${email.value}", "another-email@padles.com")) {
                    Assertions.assertEquals(HttpStatusCode.Forbidden, response.status())
                }
            }
        }

        @Test
        fun `Server should handle DELETE user##{email} - unauthenticated`() {
            val userController = mockk<UserController>(relaxed = true)
            val email = Email("user@padles.com")

            withTestApplication({ testableModule(userController) }) {
                with(handleRequest(HttpMethod.Delete, "/user/${email.value}")) {
                    Assertions.assertEquals(HttpStatusCode.Unauthorized, response.status())
                }
            }
        }
    }
}
