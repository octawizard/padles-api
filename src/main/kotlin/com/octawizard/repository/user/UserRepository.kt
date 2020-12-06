package com.octawizard.repository.user

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User

interface UserRepository {

    fun createUser(user: User): User
    fun getUser(email: Email): User?
    fun updateUser(user: User): User
    fun deleteUser(email: Email)
}
