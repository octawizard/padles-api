package com.octawizard.repository

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import java.time.Duration
import java.util.concurrent.TimeUnit

interface Cache<K, V> {

    fun get(k: K): V?

    fun put(k: K, v: V)

    fun delete(k: K)
}

class RedisCache<K, V>(redissonClient: RedissonClient, mapName: String, private val ttl: Duration) : Cache<K, V> {

    private val redisMap = redissonClient.getMapCache<K, V>(mapName)

    override fun get(k: K): V? = redisMap[k]

    override fun put(k: K, v: V) {
        redisMap.fastPut(k, v, ttl.toMillis(), TimeUnit.MILLISECONDS)
    }

    override fun delete(k: K) {
        redisMap.fastRemove(k)
    }

}

object RedissonClientFactory {

    fun create(address: String, timeout: Duration): RedissonClient {
        val config = Config()
        config.useSingleServer()
            .setTimeout(timeout.toMillis().toInt())
            .address = address

        return Redisson.create(config)
    }
}
