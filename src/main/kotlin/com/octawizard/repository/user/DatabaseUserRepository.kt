package com.octawizard.repository.user

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Gender
import com.octawizard.domain.model.User
import com.octawizard.repository.StringIdTable
import io.ktor.features.NotFoundException
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

class DatabaseUserRepository : UserRepository {
    override suspend fun createUser(user: User): User {
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

    override suspend fun getUser(email: Email): User? {
        return transaction {
            UsersEntity.findById(email.value)?.toUser()
        }
    }

    override suspend fun updateUser(user: User): User {
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

    override suspend fun deleteUser(email: Email) {
        val res = transaction {
            Users.deleteWhere { Users.id eq email.value }
        }
        if (res == 0) {
            throw NotFoundException("user ${email.value} not found")
        }
    }
}

private const val EMAIL_COLUMN_LENGTH = 250
private const val NAME_COLUMN_LENGTH = 50
private const val GENDER_COLUMN_LENGTH = 15
private const val PHONE_COLUMN_LENGTH = 16

object Users : StringIdTable("users", "email", EMAIL_COLUMN_LENGTH) {
    val name: Column<String> = varchar("name", NAME_COLUMN_LENGTH)
    val gender: Column<Gender> = enumerationByName("gender", GENDER_COLUMN_LENGTH, Gender::class)
    val phone: Column<String?> = varchar("phone", PHONE_COLUMN_LENGTH).nullable()
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
