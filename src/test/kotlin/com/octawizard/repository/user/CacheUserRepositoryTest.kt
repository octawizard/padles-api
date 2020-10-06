package com.octawizard.repository.user

import com.octawizard.domain.model.Email
import com.octawizard.domain.model.User
import com.octawizard.repository.RedisCache
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

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

        every { cache.get(email.value) } returns user

        val returnedUser = cacheUserRepository.getUser(email)

        verify { cache.get(email.value) }
        verify(inverse = true) { userRepository.getUser(email) }
        assertEquals(user, returnedUser)
    }

    @Test
    fun `CacheUserRepository should return user from repository and put it in cache if it's missing from the cache`() {
        val email = Email("email@test.com")
        val user = User(email, "test")

        every { cache.get(email.value) } returns null
        every { userRepository.getUser(email) } returns user

        val returnedUser = cacheUserRepository.getUser(email)

        verify {
            cache.get(email.value)
            userRepository.getUser(email)
            cache.put(email.value, returnedUser!!)
        }
        assertEquals(user, returnedUser)
    }

    @Test
    fun `CacheUserRepository should return missing user from repository when it's missing from the cache too`() {
        val email = Email("email@test.com")

        every { cache.get(email.value) } returns null
        every { userRepository.getUser(email) } returns null

        val returnedUser = cacheUserRepository.getUser(email)

        verify {
            cache.get(email.value)
            userRepository.getUser(email)
        }
        verify(inverse = true) { cache.put(email.value, any()) }
        assertNull(returnedUser)
    }

    @Test
    fun `CacheUserRepository should update user from repository and put the updated version in cache`() {
        val user = User(Email("email@test.com"), "test")

        every { userRepository.updateUser(user) } returns user

        val updatedUser = cacheUserRepository.updateUser(user)

        verify {
            userRepository.updateUser(user)
            cache.put(user.email.value, updatedUser)
        }
        assertEquals(user, updatedUser)
    }

    @Test
    fun `CacheUserRepository should create user from repository and put it in cache`() {
        val user = User(Email("email@test.com"), "test")

        every { userRepository.createUser(user) } returns user

        val createdUser = cacheUserRepository.createUser(user)

        verify {
            userRepository.createUser(user)
            cache.put(createdUser.email.value, createdUser)
        }
        assertEquals(user, createdUser)
    }
}
