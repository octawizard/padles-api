package com.octawizard.domain.usecase.user

import com.octawizard.domain.model.Email
import com.octawizard.repository.user.UserRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeleteUserTest {

    @Test
    fun `DeleteUser call repository to delete an user`() {
        val email = Email("test@email.com")
        val repository = mockk<UserRepository>(relaxed = true)
        val deleteUser = DeleteUser(repository)

        runBlocking { deleteUser.invoke(email) }
        coVerify(exactly = 1) { repository.deleteUser(email) }
    }
}
