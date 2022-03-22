package com.octawizard.repository.user

import com.octawizard.controller.async
import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User
import com.octawizard.repository.RedisCache
import com.octawizard.repository.retry
import kotlinx.coroutines.runBlocking

class CacheUserRepository(private val cache: RedisCache<String, User>, private val userRepository: UserRepository) :
    UserRepository {
    override suspend fun createUser(user: User): User {
        val createdUser = userRepository.createUser(user)
        retry { cache.put(createdUser.email.value, createdUser) }
        return createdUser
    }

    override suspend fun getUser(email: Email): User? {
        fun fallbackToUserRepository(email: Email): User? {
            return runBlocking {
                val user = userRepository.getUser(email).apply {
                    this?.let { async { retry { cache.put(it.email.value, it) } } }
                }

                return@runBlocking user
            }
        }

        return cache.get(email.value).takeIf { it != null } ?: fallbackToUserRepository(email)
    }

    override suspend fun updateUser(user: User): User {
        val updatedUser = userRepository.updateUser(user)
        retry { cache.put(updatedUser.email.value, updatedUser) }
        return updatedUser
    }

    override suspend fun deleteUser(email: Email) {
        userRepository.deleteUser(email).also { runBlocking { retry { cache.delete(email.value) } } }
    }
}
