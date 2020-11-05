package com.octawizard.server.route

import com.octawizard.controller.Controller
import com.octawizard.domain.model.Email
import com.octawizard.server.authorizeWithUserEmailInPath
import com.octawizard.server.input.UserInput
import com.octawizard.server.input.UserUpdateInput
import com.octawizard.server.input.toUser
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Routing.userRoutes(controller: Controller) {
    val emailParam = "email"
    authenticate {
        get("/user/{$emailParam}") {
            val emailString = call.parameters[emailParam]
                ?: throw BadRequestException("$emailParam must be present in path")
            val u = controller.getUser(emailString) ?: throw NotFoundException("User $emailString not found")
            call.respond(HttpStatusCode.OK, u)
        }

        post("/user") {
            val userToCreate = call.receive<UserInput>().toUser()
            val createdUser = controller.createUser(userToCreate)
            call.respond(HttpStatusCode.Created, createdUser)
        }

        authorizeWithUserEmailInPath(emailParam) {
            put("/user/{$emailParam}") {
                val email = Email(call.parameters[emailParam]!!) // checked in auth
                val updated = call.receive<UserUpdateInput>().toUser(email)
                val updatedUser = controller.updateUser(updated)
                    ?: throw NotFoundException("User ${email.value} not found")
                call.respond(HttpStatusCode.OK, updatedUser)
            }

            delete("/user/{$emailParam}") {
                val email = Email(call.parameters[emailParam]!!) // checked in auth
                controller.deleteUser(email)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
