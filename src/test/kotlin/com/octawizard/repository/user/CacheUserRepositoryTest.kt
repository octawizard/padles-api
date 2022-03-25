package com.octawizard.repository.user

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User
import com.octawizard.repository.RedisCache
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CacheUserRepositoryTest {
    private val cache = mockk<RedisCache<String, User>>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val cacheUserRepository = CacheUserRepository(cache, userRepository)

    @AfterEach
    fun `reset all mocks`() {
        clearAllMocks()
    }

    @Test
    fun `CacheUserRepository should return user from cache if it's present`() {
        val email = Email("email@test.com")
        val user = User(email, "test")

        coEvery { cache.get(email.value) } returns user

        val returnedUser = runBlocking {
            cacheUserRepository.getUser(email)
        }
        coVerify(timeout = 50) { cache.get(email.value) }
        coVerify(inverse = true) { userRepository.getUser(email) }
        assertEquals(user, returnedUser)
    }

    @Test
    fun `CacheUserRepository should return user from repository and put it in cache if it's missing from the cache`() {
        val email = Email("email@test.com")
        val user = User(email, "test")

        coEvery { cache.get(email.value) } returns null
        coEvery { userRepository.getUser(email) } returns user

        val returnedUser = runBlocking { cacheUserRepository.getUser(email) }

        coVerify(timeout = 50) {
            cache.get(email.value)
            userRepository.getUser(email)
            cache.put(email.value, returnedUser!!)
        }
        assertEquals(user, returnedUser)
    }

    @Test
    fun `CacheUserRepository should return missing user from repository when it's missing from the cache too`() {
        val email = Email("email@test.com")

        coEvery { cache.get(email.value) } returns null
        coEvery { userRepository.getUser(email) } returns null

        val returnedUser = runBlocking {
            cacheUserRepository.getUser(email)
        }

        coVerify(timeout = 50) {
            cache.get(email.value)
            userRepository.getUser(email)
        }
        coVerify(inverse = true, timeout = 50) { cache.put(email.value, any()) }
        assertNull(returnedUser)
    }

    @Test
    fun `CacheUserRepository should update user from repository and put the updated version in cache`() {
        val user = User(Email("email@test.com"), "test")

        coEvery { userRepository.updateUser(user) } returns user

        val updatedUser = runBlocking { cacheUserRepository.updateUser(user) }

        coVerify(timeout = 50) {
            userRepository.updateUser(user)
            cache.put(user.email.value, updatedUser)
        }
        assertEquals(user, updatedUser)
    }

    @Test
    fun `CacheUserRepository should create user from repository and put it in cache`() {
        val user = User(Email("email@test.com"), "test")

        coEvery { userRepository.createUser(user) } returns user

        val createdUser = runBlocking { cacheUserRepository.createUser(user) }

        coVerify(timeout = 50) {
            userRepository.createUser(user)
            cache.put(createdUser.email.value, createdUser)
        }
        assertEquals(user, createdUser)
    }

    @Test
    fun `CacheUserRepository should delete a user from repository and cache`() {
        val user = User(Email("email@test.com"), "test")

        coEvery { userRepository.createUser(user) } returns user

        runBlocking { cacheUserRepository.deleteUser(user.email) }

        coVerify(timeout = 50) {
            userRepository.deleteUser(user.email)
            cache.delete(user.email.value)
        }
    }
}
