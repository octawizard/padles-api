package com.octawizard.repository.user

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User
import com.octawizard.repository.StringIdTable
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class DatabaseUserRepository : UserRepository {
    override fun createUser(user: User): User {
        transaction {
            Users.insert {
                it[id] = EntityID(user.email.value, Users)
                it[name] = user.name
                it[createdAt] = user.createdAt
            }
        }
        return user
    }

    override fun getUser(email: Email): User? {
        return transaction {
            UsersEntity.findById(email.value)?.toUser()
        }
    }

    override fun updateUser(user: User): User {
        transaction {
            Users.update({ Users.id eq user.email.value }) {
                it[id] = EntityID(user.email.value, Users)
                it[name] = user.name
                it[createdAt] = user.createdAt
            }
        }
        return user
    }
}

object Users : StringIdTable("users", "email", 250) {
    val name: Column<String> = varchar("name", 50)
    val createdAt: Column<LocalDateTime> = datetime("created_at")
}

class UsersEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, UsersEntity>(Users)

    var name by Users.name
    var createdAt by Users.createdAt

    fun toUser(): User = User(Email(id.value), name, createdAt)
}
