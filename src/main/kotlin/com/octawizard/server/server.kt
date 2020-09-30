package com.octawizard.server

import com.octawizard.controller.Controller
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User
import com.octawizard.domain.usecase.useCaseModule
import com.octawizard.repository.Users
import com.octawizard.repository.repositoryModule
import com.octawizard.server.input.UserInput
import com.octawizard.server.input.UserUpdateInput
import com.octawizard.server.input.toUser
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
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.*
import java.time.LocalDateTime


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

fun main() {
    val kodein = DI {
        import(useCaseModule)
        import(repositoryModule)
        bind<Controller>() with singleton { Controller(instance(), instance(), instance(), instance(), instance(), instance()) }
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
        SchemaUtils.create(Users)
        SchemaUtils.createMissingTablesAndColumns(Users)
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
