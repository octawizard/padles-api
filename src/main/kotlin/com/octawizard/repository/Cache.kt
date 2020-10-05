package com.octawizard.repository

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import java.util.concurrent.TimeUnit

interface Cache<K, V> {

    fun get(k: K): V?

    fun put(k: K, v: V)
}

class RedisCache<K, V>(redissonClient: RedissonClient, mapName: String, private val ttl: Long, private val ttlUnit: TimeUnit) : Cache<K, V> {

    private val redisMap = redissonClient.getMapCache<K, V>(mapName)

    override fun get(k: K): V? = redisMap[k]

    override fun put(k: K, v: V) {
        redisMap.put(k, v, ttl, ttlUnit)
    }

}

object RedissonClientFactory {

    fun create(address: String, timeoutInMs: Int): RedissonClient {
        val config = Config()
        config.useSingleServer()
                .setTimeout(timeoutInMs)
                .address = address

        return Redisson.create(config)
    }
}
