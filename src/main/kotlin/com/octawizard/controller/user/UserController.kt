package com.octawizard.controller.user

import com.octawizard.controller.async
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User
import com.octawizard.domain.usecase.user.CreateUser
import com.octawizard.domain.usecase.user.DeleteUser
import com.octawizard.domain.usecase.user.GetUser
import com.octawizard.domain.usecase.user.UpdateUser

class UserController(
    private val createUser: CreateUser,
    private val updateUser: UpdateUser,
    private val deleteUser: DeleteUser,
    private val getUser: GetUser,
) {

    suspend fun createUser(user: User): User = async { createUser.invoke(user) }

    suspend fun getUser(emailString: String): User? {
        val email = Email(emailString)
        return async { getUser(email) }
    }

    suspend fun updateUser(updated: User): User? {
        return async {
            getUser(updated.email)?.email?.let { updateUser.invoke(updated) }
        }
    }

    suspend fun deleteUser(email: Email) {
        return async { deleteUser.invoke(email) }
    }
}
