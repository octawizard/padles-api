package com.octawizard.repository.user

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.Gender
import com.octawizard.domain.model.User
import io.ktor.features.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseUserRepositoryTest {
    private lateinit var repository: DatabaseUserRepository

    @BeforeAll
    fun `init database`() {
        Database.connect(
                url = "jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver",
                user = "test",
                password = "")


        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users)
        }
        repository = DatabaseUserRepository()
    }

    @AfterEach
    fun `clean database`() {
        transaction {
            Users.deleteAll()
        }
    }

    @Test
    fun `DatabaseUserRepository creates and returns a user`() {
        val user = User(Email("user@padles.com"), "tony")
        assertEquals(user, repository.createUser(user))
    }

    @Test
    fun `DatabaseUserRepository returns a null user when user is not found`() {
        assertNull(repository.getUser(Email("test@test.com")))
    }

    @Test
    fun `DatabaseUserRepository returns an user when user is found`() {
        val email = Email("test@test.com")
        transaction {
            UsersEntity.new(email.value) {
                name = "Test User"
                gender = Gender.Other
                createdAt = LocalDateTime.now()
            }
        }
        assertEquals(email, repository.getUser(email)?.email)
    }

    @Test
    fun `DatabaseUserRepository returns updated user when user is updated`() {
        val email = Email("test@test.com")
        val createdAt = LocalDateTime.now()
        val previousName = "Test User"
        val updatedName = "Test User Updated"
        val gender = Gender.Female
        val phone = "phone"
        val user = User(email, updatedName, gender, phone, createdAt)
        transaction {
            UsersEntity.new(email.value) {
                name = previousName
                this.gender = gender
                this.createdAt = createdAt
            }
        }
        val updatedUser = repository.updateUser(user)
        assertEquals(email, updatedUser.email)
        assertEquals(updatedName, updatedUser.name)
        assertEquals(gender, updatedUser.gender)
        assertNotEquals(previousName, updatedUser.name)
        assertEquals(createdAt, updatedUser.createdAt)
    }

    @Test
    fun `DatabaseUserRepository returns exception when trying to update not existing user`() {
        val email = Email("test@test.com")
        val createdAt = LocalDateTime.now()
        val updatedName = "Test User Updated"
        val phone = "phone"
        val user = User(email, updatedName, Gender.Female, phone, createdAt)

        assertThrows(NotFoundException::class.java) { repository.updateUser(user)  }
    }

    @Test
    fun `DatabaseUserRepository should delete a user if exists`() {
        val user = User(Email("user@padles.com"), "tony")
        repository.createUser(user)

        repository.deleteUser(user.email)

        assertNull(repository.getUser(user.email))
    }

    @Test
    fun `DatabaseUserRepository should throw an exception when deleting a user that doesn't exist`() {
        assertThrows(NotFoundException::class.java) { repository.deleteUser(Email("test@test.com"))  }
    }
}
