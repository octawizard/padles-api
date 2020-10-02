package com.octawizard.repository

import org.redisson.Redisson
import org.redisson.api.RMap
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import java.util.concurrent.TimeUnit

interface Cache<K, V> {

    fun get(k: K): V?

    fun put(k: K, v: V)
}

class RedisCache<K, V>(redissonClient: RedissonClient, mapName: String, ttl: Long, ttlUnit: TimeUnit) : Cache<K, V> {

    private val redisMap: RMap<K, V> = redissonClient.getMap(mapName)

    init {
        redisMap.expire(ttl, ttlUnit)
    }

    override fun get(k: K): V? = redisMap[k]

    override fun put(k: K, v: V) {
        redisMap[k] = v
    }

}

object RedissonClientFactory {

    fun create(address: String): RedissonClient {
        // todo read from config
        val config = Config()
        config.useSingleServer().address = address

        return Redisson.create(config)
    }
}
