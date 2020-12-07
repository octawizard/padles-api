package com.octawizard.controller.user

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User
import com.octawizard.domain.usecase.user.CreateUser
import com.octawizard.domain.usecase.user.DeleteUser
import com.octawizard.domain.usecase.user.GetUser
import com.octawizard.domain.usecase.user.UpdateUser
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {
    private val createUser: CreateUser = mockk()
    private val updateUser: UpdateUser = mockk()
    private val deleteUser: DeleteUser = mockk(relaxed = true)
    private val getUser: GetUser = mockk()
    private val controller = UserController(createUser, updateUser, deleteUser, getUser)

    @AfterEach
    fun `reset mocks`() {
        clearAllMocks()
    }

    @Test
    fun `UserController should call CreateUser to create a user`() {
        val user = mockk<User>()
        every { createUser(any()) } returns user

        assertEquals(user, runBlocking { controller.createUser(user) })
        verify { createUser(user) }
    }

    @Test
    fun `UserController should call GetUser to get a user`() {
        val user = mockk<User>()
        every { getUser(any()) } returns user
        val emailString = "test@padles.com"

        assertEquals(user, runBlocking { controller.getUser(emailString) })
        verify { getUser(Email(emailString)) }
    }

    @Test
    fun `UserController should call UpdateUser to update a user`() {
        val user = User(Email("test@padles.com"), "user")
        every { updateUser(any()) } returns user
        every { getUser(any()) } returns user

        assertEquals(user, runBlocking { controller.updateUser(user) })
        verify {
            getUser(user.email)
            updateUser(user)
        }
    }

    @Test
    fun `UserController should call UpdateUser to update a user when user is not found`() {
        val user = User(Email("test@padles.com"), "user")
        every { updateUser(any()) } returns user
        every { getUser(any()) } returns null

        assertEquals(null, runBlocking { controller.updateUser(user) })
        verify {
            getUser(user.email)
        }
        verify(inverse = true) {
            updateUser(user)
        }
    }

    @Test
    fun `UserController should call DeleteUser to delete a user`() {
        val email = Email("test@padles.com")

        runBlocking { controller.deleteUser(email) }

        verify { deleteUser(email) }
    }
}
