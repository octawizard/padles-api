package com.octawizard.repository.user

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User
import com.octawizard.repository.RedisCache

class CacheUserRepository(private val cache: RedisCache<String, User>, private val userRepository: UserRepository) : UserRepository {
    override fun createUser(user: User): User {
        val createdUser = userRepository.createUser(user)
        cache.put(createdUser.email.value, createdUser)
        return createdUser
    }

    override fun getUser(email: Email): User? {
        fun fallbackToUserRepository(email: Email): User? {
            val user = userRepository.getUser(email)
            user?.let { cache.put(it.email.value, it) }
            return user
        }

        return cache.get(email.value).takeIf { it != null } ?: fallbackToUserRepository(email)
    }

    override fun updateUser(user: User): User {
        val updatedUser = userRepository.updateUser(user)
        cache.put(updatedUser.email.value, updatedUser)
        return updatedUser
    }
}
