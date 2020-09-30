package com.octawizard.repository

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class DatabaseUserRepository : UserRepository {
    override fun createUser(user: User): User {
        transaction {
            Users.insert {
                it[email] = user.email.value
                it[name] = user.name
                it[createdAt] = user.createdAt
            }
        }
        return user
    }

    override fun getUser(email: Email): User? {
        return transaction {
            Users.select { Users.email eq email.value }
                .map { User(Email(it[Users.email]), it[Users.name], it[Users.createdAt]) }
                .firstOrNull()
        }
    }

    override fun updateUser(user: User): User {
        transaction {
            Users.update({ Users.email eq user.email.value }) {
                it[email] = user.email.value
                it[name] = user.name
                it[createdAt] = user.createdAt
            }
        }
        return user
    }
}

object Users : Table("users") {
    val email: Column<String> = varchar("email", 250)
    val name: Column<String> = varchar("name", 50)
    val createdAt: Column<LocalDateTime> = datetime("created_at")

    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(email) }
}
