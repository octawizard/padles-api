package com.octawizard.repository

import com.typesafe.config.ConfigFactory
import java.time.Duration

interface ConfigurationFactory<T> {
    fun build(): T
}

data class DatabaseConfiguration(
    val jdbcUrl: String,
    val dbDriverClassName: String,
    val dbUsername: String,
    val dbPassword: String,
    val dbMaximumPoolSize: Int,
)

object DatabaseConfigurationFactory : ConfigurationFactory<DatabaseConfiguration> {

    override fun build(): DatabaseConfiguration {
        val config = ConfigFactory.load()
        return DatabaseConfiguration(
            jdbcUrl = config.getString("database.jdbc_url"),
            dbDriverClassName = config.getString("database.driver_class_name"),
            dbUsername = config.getString("database.username"),
            dbPassword = config.getString("database.password"),
            dbMaximumPoolSize = config.getInt("database.max_pool_size"),
        )
    }
}

data class MongoRepositoryConfiguration(
    val host: String,
    val port: Int,
    val database: String,
    val clubsCollectionName: String,
    val reservationsCollectionName: String,
) {
    val connectionString: String = "mongodb://$host:$port"
}

object MongoRepositoryConfigurationFactory : ConfigurationFactory<MongoRepositoryConfiguration> {

    override fun build(): MongoRepositoryConfiguration {
        val config = ConfigFactory.load()
        return MongoRepositoryConfiguration(
            host = config.getString("mongo.host"),
            port = config.getInt("mongo.port"),
            database = config.getString("mongo.database"),
            clubsCollectionName = config.getString("mongo.clubsCollectionName"),
            reservationsCollectionName = config.getString("mongo.reservationsCollectionName"),
        )
    }
}

data class RedisRepositoryConfiguration(
    val protocol: String,
    val host: String,
    val port: Int,
    val timeout: Duration,
    val userCacheTtl: Duration,
    val userCacheName: String,
)

object RedisRepositoryConfigurationFactory : ConfigurationFactory<RedisRepositoryConfiguration> {
    override fun build(): RedisRepositoryConfiguration {
        val config = ConfigFactory.load()
        return RedisRepositoryConfiguration(
            protocol = config.getString("redis.protocol"),
            host = config.getString("redis.host"),
            port = config.getInt("redis.port"),
            timeout = config.getDuration("redis.timeout"),
            userCacheTtl = config.getDuration("redis.map.user.ttl"),
            userCacheName = config.getString("redis.map.user.name"),
        )
    }

}
