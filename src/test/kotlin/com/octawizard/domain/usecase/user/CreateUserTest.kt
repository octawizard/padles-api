package com.octawizard.domain.usecase.user

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User
import com.octawizard.repository.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateUserTest {

    @Test
    fun `CreateUser call repository to create and return a user`() {
        val email = Email("test@email.com")
        val user = User(email, "test")
        val repository = mockk<UserRepository>(relaxed = true)
        val createUser = CreateUser(repository)

        every { repository.createUser(user) } returns user

        assertEquals(user, createUser.invoke(user))
        verify(exactly = 1) { repository.createUser(user) }
    }
}
