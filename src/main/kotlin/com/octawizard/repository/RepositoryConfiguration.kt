package com.octawizard.repository

import com.typesafe.config.ConfigFactory
import java.time.Duration

data class RepositoryConfiguration(
        val protocol: String = "redis",
        val host: String = "localhost",
        val port: Int = 6379,
        val timeout: Duration,
        val userCacheTtl: Duration,
        val matchCacheTtl: Duration,
        val jdbcUrl: String,
        val dbDriverClassName: String,
        val dbUsername: String,
        val dbPassword: String,
        val dbMaximumPoolSize: Int
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

        val jdbcUrl = config.getString("database.jdbc_url")
        val dbDriverClassName = config.getString("database.driver_class_name")
        val dbUsername = config.getString("database.username")
        val dbPassword = config.getString("database.password")
        val dbMaximumPoolSize = config.getInt("database.max_pool_size")

        return RepositoryConfiguration(
                protocol, host, port, timeout, userCacheTtl, matchCacheTtl, jdbcUrl, dbDriverClassName, dbUsername,
                dbPassword, dbMaximumPoolSize
        )
    }
}
