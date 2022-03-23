package com.octawizard.server.route

import com.octawizard.controller.user.UserController
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.isValidEmail
import com.octawizard.server.USER_BASED_AUTH_CONFIG
import com.octawizard.server.authorizeWithUserEmailInPath
import com.octawizard.server.input.CreateUserInput
import com.octawizard.server.input.UserUpdateInput
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.post

const val USER_EMAIL_PARAM = "userEmail"

@KtorExperimentalLocationsAPI
fun Routing.userRoutes(controller: UserController) {

    @Location("/user/{$USER_EMAIL_PARAM}")
    data class UserRoute(val userEmail: String)

    authenticate(USER_BASED_AUTH_CONFIG) {

        get<UserRoute> { route ->
            check(route.userEmail.isValidEmail())
            val user = controller.getUser(route.userEmail) ?: entityNotFound(route.userEmail)
            call.respond(HttpStatusCode.OK, user)
        }

        post("/user") {
            val userToCreate = call.receive<CreateUserInput>().toUser()
            val createdUser = controller.createUser(userToCreate)
            call.respond(HttpStatusCode.Created, createdUser)
        }

        authorizeWithUserEmailInPath {

            put<UserRoute> { route ->
                check(route.userEmail.isValidEmail())
                val email = Email(route.userEmail)
                val updated = call.receive<UserUpdateInput>().toUser(email)
                val updatedUser = controller.updateUser(updated) ?: entityNotFound(email.value)
                call.respond(HttpStatusCode.OK, updatedUser)
            }

            delete<UserRoute> { route ->
                check(route.userEmail.isValidEmail())
                val email = Email(route.userEmail)
                controller.deleteUser(email)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
