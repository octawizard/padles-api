package com.octawizard.repository

import com.typesafe.config.ConfigFactory
import java.time.Duration

data class RepositoryConfiguration(
    val protocol: String = "redis",
    val host: String = "localhost",
    val port: Int = 6379,
    val timeout: Duration,
val userCacheTtl: Duration,
val matchCacheTtl: Duration
)

object RepositoryConfigurationFactory {

    fun build(): RepositoryConfiguration {
        val config = ConfigFactory.load()
        val protocol = config.getString("redis.protocol")
        val host = config.getString("redis.host")
        val port = config.getInt("redis.port")
        val timeout = config.getDuration("redis.timeout")
        val userCacheTtl = config.getDuration("redis.map.user.ttl")
        val matchCacheTtl = config.getDuration("redis.map.match.ttl")
        return RepositoryConfiguration(protocol, host, port, timeout, userCacheTtl, matchCacheTtl)
    }
}
