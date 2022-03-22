package com.octawizard.repository.user

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User

interface UserRepository {

    suspend fun createUser(user: User): User
    suspend fun getUser(email: Email): User?
    suspend fun updateUser(user: User): User
    suspend fun deleteUser(email: Email)
}
