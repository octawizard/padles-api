package com.octawizard.repository.user

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Gender
import com.octawizard.domain.model.User
import com.octawizard.repository.StringIdTable
import io.ktor.features.*
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
                it[gender] = user.gender
                it[phone] = user.phone
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
        val updated = transaction {
            Users.update({ Users.id eq user.email.value }) {
                it[id] = EntityID(user.email.value, Users)
                it[name] = user.name
                it[gender] = user.gender
                it[phone] = user.phone
                it[createdAt] = user.createdAt
            }
        }
        return when (updated) {
            1 -> user
            else -> throw NotFoundException("user ${user.email} not found, cannot update")
        }
    }

    override fun deleteUser(email: Email) {
        val res = transaction {
            Users.deleteWhere { Users.id eq email.value }
        }
        if (res == 0) {
            throw NotFoundException("user ${email.value} not found")
        }
    }
}

object Users : StringIdTable("users", "email", 250) {
    val name: Column<String> = varchar("name", 50)
    val gender: Column<Gender> = enumerationByName("gender",15, Gender::class)
    val phone: Column<String?> = varchar("phone", 16).nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at")
}

class UsersEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, UsersEntity>(Users)

    var name by Users.name
    var gender by Users.gender
    var phone by Users.phone
    var createdAt by Users.createdAt

    fun toUser(): User = User(Email(id.value), name, gender, phone, createdAt)
}
