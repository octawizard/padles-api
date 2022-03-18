package com.octawizard.repository

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.redisson.api.RMapCache
import org.redisson.api.RedissonClient
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.test.assertNotNull


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CacheTest {
    private val timeout = Duration.ofSeconds(1)

    @Test
    fun `RedissonClientFactory throws an exception when creating a RedissonClient given an invalid address`() {
        assertThrows(IllegalArgumentException::class.java) { RedissonClientFactory.create("", timeout) }
    }

    @Test
    fun `RedisCache create`() {
        val client = mockk<RedissonClient>()
        val mapName = "name"
        every { client.getMapCache<Int, Int>(mapName)} returns mockk<RMapCache<Int, Int>>()

        assertNotNull(RedisCache.create<Int, Int>(client, mapName, timeout))
    }

    @Test
    fun `RedisCache get`() {
        val ttl = Duration.ofMillis(500)
        val key = 1
        val expectedValue = "value"
        val redisMap = mockk<RMapCache<Int, String>>()
        val redisCache = RedisCache(redisMap, ttl)

        every { redisCache.get(key) } returns expectedValue

        val result = redisCache.get(key)

        assertEquals(expectedValue, result)
        verify { redisMap[key] }
    }

    @Test
    fun `RedisCache put`() {
        val ttl = Duration.ofMillis(500)
        val key = 1
        val value = "value"
        val redisMap = mockk<RMapCache<Int, String>>()
        val redisCache = RedisCache(redisMap, ttl)

        every { redisMap.fastPut(any(), any(), any(), any()) } returns true

        redisCache.put(key, value)

        verify { redisMap.fastPut(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS) }
    }

    @Test
    fun `RedisCache delete`() {
        val ttl = Duration.ofMillis(500)
        val key = 1
        val redisMap = mockk<RMapCache<Int, String>>()
        val redisCache = RedisCache(redisMap, ttl)

        every { redisMap.fastRemove(any()) } returns 1

        redisCache.delete(key)

        verify { redisMap.fastRemove(key) }
    }
}
