package com.octawizard.server.route

import com.octawizard.controller.user.UserController
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.isValidEmail
import com.octawizard.server.UserBasedAuthenticationConfig
import com.octawizard.server.authorizeWithUserEmailInPath
import com.octawizard.server.input.CreateUserInput
import com.octawizard.server.input.UserUpdateInput
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.locations.put
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

const val UserEmailParam = "userEmail"

@KtorExperimentalLocationsAPI
fun Routing.userRoutes(controller: UserController) {

    @Location("/user/{$UserEmailParam}")
    data class UserRoute(val userEmail: String)

    authenticate(UserBasedAuthenticationConfig) {

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
