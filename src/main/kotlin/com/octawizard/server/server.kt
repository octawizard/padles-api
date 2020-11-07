package com.octawizard.server

import com.octawizard.controller.Controller
import com.octawizard.domain.usecase.useCaseModule
import com.octawizard.repository.repositoryConfigurationModule
import com.octawizard.repository.repositoryModule
import com.octawizard.server.route.clubRoutes
import com.octawizard.server.route.reservationRoutes
import com.octawizard.server.route.userRoutes
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.locations.*
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
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
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
        install(Locations)
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
            with(controller) {
                userRoutes(this)
                reservationRoutes(this)
                clubRoutes(this)
            }
        }
    }.start(wait = true)
}
