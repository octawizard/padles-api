package com.octawizard.domain.usecase.user

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User
import com.octawizard.repository.user.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetUserTest {

    @Test
    fun `GetUser call repository to get a user`() {
        val email = Email("test@email.com")
        val user = User(email, "test")
        val repository = mockk<UserRepository>(relaxed = true)
        val getUser = GetUser(repository)

        coEvery { repository.getUser(email) } returns user

        assertEquals(user, runBlocking { getUser.invoke(email) })
        coVerify(exactly = 1) { repository.getUser(email) }
    }
}
