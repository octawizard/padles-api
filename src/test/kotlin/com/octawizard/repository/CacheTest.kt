package com.octawizard.repository

import kotlinx.coroutines.runBlocking
import org.junit.ClassRule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.GenericContainer
import java.time.Duration
import java.util.concurrent.TimeUnit


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CacheTest {
    var host: String = ""
    var port: Int = 1234
    private val timeout = Duration.ofSeconds(1)

    companion object {
        @get:ClassRule
        @JvmStatic
        val redis = object : GenericContainer<Nothing>("redis:6.0.8-alpine") {
            init {
                withExposedPorts(6379)
            }
        }
    }


    @BeforeAll
    fun `initialize local Redis server`() {
        redis.start()
        host = redis.host
        port = redis.getMappedPort(6379)
    }

    @AfterAll
    fun `shutdown Redis`() {
        redis.stop()
    }

    @Test
    fun `RedissonClientFactory returns a RedissonClient given a valid address`() {
        val client = RedissonClientFactory.create("redis://$host:$port", timeout)
        assertFalse(client.isShutdown)
    }

    @Test
    fun `RedissonClientFactory throws an exception when creating a RedissonClient given an invalid address`() {
        assertThrows(IllegalArgumentException::class.java) { RedissonClientFactory.create("", timeout) }
    }

    @Test
    fun `RedisCache store and read value in cache`() {
        val client = RedissonClientFactory.create("redis://$host:$port", timeout)
        val ttl = Duration.ofMillis(500)
        val redisCache = RedisCache<Int, String>(client, "putTest", ttl)

        val key = 1
        assertNull(redisCache.get(key))
        val value = key.toString()
        redisCache.put(key, value)
        assertEquals(value, redisCache.get(key))
    }

    @Test
    fun `RedisCache expires value after given ttl`() {
        val client = RedissonClientFactory.create("redis://$host:$port", timeout)
        val ttl = Duration.ofMillis(100)
        val redisCache = RedisCache<Int, String>(client, "expireTest", ttl)

        val key = 1
        val value = key.toString()
        redisCache.put(key, value)
        runBlocking { Thread.sleep(ttl.toMillis()) }

        assertNull(redisCache.get(key))
    }

    @Test
    fun `RedisCache delete value given a key`() {
        val client = RedissonClientFactory.create("redis://$host:$port", timeout)
        val ttl = Duration.ofMillis(100)
        val redisCache = RedisCache<Int, String>(client, "deleteTest", ttl)

        val key = 1
        val value = key.toString()
        redisCache.put(key, value)
        assertEquals(value, redisCache.get(key))

        redisCache.delete(key)

        assertNull(redisCache.get(key))
    }
}
