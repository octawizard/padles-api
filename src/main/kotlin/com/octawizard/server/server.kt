package com.octawizard.server

import com.octawizard.controller.Controller
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User
import com.octawizard.domain.usecase.useCaseModule
import com.octawizard.repository.match.Matches
import com.octawizard.repository.user.Users
import com.octawizard.repository.repositoryModule
import com.octawizard.server.input.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.html.*
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.*
import java.util.*


fun HTML.index(user: User) {
    head {
        title("Hello from Ktor!")
    }
    body {
        div {
            +"User created $user"
        }
    }
}
private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Hello world" }
    val kodein = DI {
        import(useCaseModule)
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
                instance()
            )
        }
    }

    initDatabase()

    val controller = kodein.direct.instance<Controller>()

    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        install(ContentNegotiation) {
            gson { }
        }
        install(DefaultHeaders)
        install(CallLogging)
        routing {
            userRoutes(controller)
            matchRoutes(controller)
        }
    }.start(wait = true)
}

private fun initDatabase() {
    // todo read this from config
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://localhost:5432/padles"
        driverClassName = "org.postgresql.Driver"
        username = "padles_admin"
        password = "local"
        maximumPoolSize = 10
    }
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(Users, Matches)
        SchemaUtils.createMissingTablesAndColumns(Users, Matches)
    }
}

private fun Routing.userRoutes(controller: Controller) {
    val emailParam = "email"

    get("/user/{$emailParam}") {
        val emailString = call.parameters[emailParam]
            ?: throw BadRequestException("$emailParam must be present in path")
        val u = controller.getUser(emailString) ?: throw NotFoundException("User $emailString not found")
        call.respond(HttpStatusCode.OK, u)
    }


    // all matches where the user plays/played
    get("/user/{$emailParam}/match") {
        TODO("to implement")
    }

    post("/user") {
        val userToCreate = call.receive<UserInput>().toUser()
        val createdUser = controller.createUser(userToCreate)
        call.respond(HttpStatusCode.Created, createdUser)
    }

    put("/user/{$emailParam}") {
        val emailString = call.parameters[emailParam]
            ?: throw BadRequestException("$emailParam must be present in path")
        val email = Email(emailString)
        val updated = call.receive<UserUpdateInput>().toUser(email)
        val updatedUser = controller.updateUser(updated) ?: throw NotFoundException("User $emailString not found")
        call.respond(HttpStatusCode.OK, updatedUser)
    }
}

private fun Routing.matchRoutes(controller: Controller) {
    val matchId = "matchId"

    post("/match") {
        val input = call.receive<CreateMatchInput>()
        val createdMatch = controller.createMatch(input.player1, input.player2, input.player3, input.player4)
        call.respond(HttpStatusCode.Created, createdMatch)
    }

    get("/match/{$matchId}") {
        val inputMatchId = UUID.fromString(call.parameters[matchId])
        val match = controller.getMatch(inputMatchId) ?: throw NotFoundException()
        call.respond(HttpStatusCode.OK, match)
    }

    patch("/match/{$matchId}") {
        val input = call.receive<PatchMatchInput>()
        val inputMatchId = UUID.fromString(call.parameters[matchId])
        val updateMatch = controller.patchMatch(input, inputMatchId) ?: throw NotFoundException()
        call.respond(HttpStatusCode.OK, updateMatch)
    }

    delete("/match/{$matchId}"){
        val inputMatchId = UUID.fromString(call.parameters[matchId])
        controller.deleteMatch(inputMatchId)
        call.respond(HttpStatusCode.NoContent)
    }

    // all matches that needs at least one player
    get("/match") {
        call.respond(HttpStatusCode.OK, controller.getAllAvailableMatches())
    }
}
