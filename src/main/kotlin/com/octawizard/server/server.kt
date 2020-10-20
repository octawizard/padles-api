package com.octawizard.server

import com.octawizard.controller.Controller
import com.octawizard.domain.usecase.useCaseModule
import com.octawizard.repository.repositoryConfigurationModule
import com.octawizard.repository.repositoryModule
import com.octawizard.server.route.reservationRoutes
import com.octawizard.server.route.userRoutes
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.singleton

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Hello world" }
    val kodein = DI {
        import(useCaseModule)
        import(repositoryConfigurationModule)
        import(repositoryModule)
        bind<Controller>() with singleton {
            Controller(
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance(),
                    instance()
            )
        }
    }

    val controller = kodein.direct.instance<Controller>()

    embeddedServer(Netty, port = 1111, host = "127.0.0.1") {
        install(ContentNegotiation) {
            gson { }
        }
        install(DefaultHeaders)
        install(CallLogging)
        install(Authentication) {
            authenticationConfig()
        }
        install(UserEmailBasedAuthorization)
        install(StatusPages) {
            exception<AuthorizationException> {
                call.response.status(HttpStatusCode.Forbidden)
            }
        }
        routing {
            userRoutes(controller)
//            matchRoutes(controller)
            reservationRoutes(controller)
        }
    }.start(wait = true)
}

//private fun Routing.matchRoutes(controller: Controller) {
//    val matchId = "matchId"
//
//    post("/match") {
//        val input = call.receive<CreateMatchInput>()
//        val createdMatch = controller.createMatch(input.player1, input.player2, input.player3, input.player4)
//        call.respond(HttpStatusCode.Created, createdMatch)
//    }
//
//    get("/match/{$matchId}") {
//        val inputMatchId = UUID.fromString(call.parameters[matchId])
//        val match = controller.getMatch(inputMatchId) ?: throw NotFoundException()
//        call.respond(HttpStatusCode.OK, match)
//    }
//
//    patch("/match/{$matchId}") {
//        val input = call.receive<PatchMatchInput>()
//        val inputMatchId = UUID.fromString(call.parameters[matchId])
//        val updateMatch = controller.patchMatch(input, inputMatchId) ?: throw NotFoundException()
//        call.respond(HttpStatusCode.OK, updateMatch)
//    }
//
//    delete("/match/{$matchId}") {
//        val inputMatchId = UUID.fromString(call.parameters[matchId])
//        controller.deleteMatch(inputMatchId)
//        call.response.status(HttpStatusCode.NoContent)
//    }
//
//    // all matches that needs at least one player
//    get("/match") {
//        call.respond(HttpStatusCode.OK, controller.getAllAvailableMatches())
//    }
//}
