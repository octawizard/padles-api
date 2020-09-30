package com.octawizard.repository

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User

interface UserRepository {

    fun createUser(user: User): User
    fun getUser(email: Email): User?
    fun updateUser(user: User): User
}

class InMemoryUserRepository: UserRepository {

    val users = mutableMapOf<Email, User>()

    override fun createUser(user: User): User {
        users[user.email] = user
        return user
    }

    override fun getUser(email: Email): User? {
        return users[email]
    }

    override fun updateUser(user: User): User {
        users[user.email] = user
        return user
    }
}
