package com.octawizard.server

import com.octawizard.controller.club.ClubController
import com.octawizard.controller.controllerModule
import com.octawizard.controller.reservation.ReservationController
import com.octawizard.controller.user.UserController
import com.octawizard.domain.usecase.useCaseModule
import com.octawizard.repository.repositoryConfigurationModule
import com.octawizard.repository.repositoryModule
import com.octawizard.server.route.clubRoutes
import com.octawizard.server.route.reservationRoutes
import com.octawizard.server.route.userRoutes
import com.octawizard.server.serialization.contextualSerializers
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Starting padles-api" }
    val kodein = DI {
        import(controllerModule)
        import(useCaseModule)
        import(repositoryConfigurationModule)
        import(repositoryModule)
    }

    val reservationController = kodein.direct.instance<ReservationController>()
    val userController = kodein.direct.instance<UserController>()
    val clubController = kodein.direct.instance<ClubController>()

    embeddedServer(Netty, port = 1111, host = "127.0.0.1") {
        install(ContentNegotiation) {
            json(Json {
                serializersModule = contextualSerializers
                allowStructuredMapKeys = true
            })
        }
        install(DefaultHeaders)
        install(CallLogging)
        install(Locations)
        install(Authentication) {
            authenticationConfig()
        }
        install(UserEmailBasedAuthorization)
        install(StatusPages) {
            exceptionHandler()
        }
        routing {
            userRoutes(userController)
            clubRoutes(clubController)
            reservationRoutes(reservationController)
        }
    }.start(wait = true)
}

private fun StatusPages.Configuration.exceptionHandler() {
    exception<AuthorizationException> {
        call.response.status(HttpStatusCode.Forbidden)
    }
    exception<IllegalStateException> { cause ->
        call.respond(HttpStatusCode.BadRequest, cause.localizedMessage)
    }
    exception<SerializationException> { cause ->
        call.respond(HttpStatusCode.BadRequest, cause.localizedMessage)
    }
}

