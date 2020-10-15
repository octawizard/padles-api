package com.octawizard.repository

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.User
import com.octawizard.repository.match.CacheMatchRepository
import com.octawizard.repository.match.DatabaseMatchRepository
import com.octawizard.repository.match.MatchRepository
import com.octawizard.repository.user.CacheUserRepository
import com.octawizard.repository.user.DatabaseUserRepository
import com.octawizard.repository.user.UserRepository
import com.octawizard.repository.user.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.eagerSingleton
import org.kodein.di.instance
import org.kodein.di.singleton
import org.redisson.api.RedissonClient
import java.util.*
import javax.sql.DataSource

val repositoryModule = DI.Module("repository") {
    bind<RedisCache<String, User>>(tag = "userCache") with
            singleton { RedisCache(instance(), "users", instance<RepositoryConfiguration>().userCacheTtl) }
    bind<RedisCache<UUID, Match>>(tag = "matchCache") with
            singleton { RedisCache(instance(), "matches", instance<RepositoryConfiguration>().matchCacheTtl) }

    bind<UserRepository>(tag = "db") with singleton { DatabaseUserRepository() }
    bind<UserRepository>() with
            singleton { CacheUserRepository(instance("userCache"), instance("db")) }
    bind<MatchRepository>(tag = "db") with singleton { DatabaseMatchRepository() }
    bind<MatchRepository>() with singleton { CacheMatchRepository(instance("matchCache"), instance("db")) }
    bind<RedissonClient>() with singleton {
        val config = instance<RepositoryConfiguration>()
        val address = "${config.protocol}://${config.host}:${config.port}"
        RedissonClientFactory.create(address, config.timeout)
    }

    bind<DataSource>() with singleton {
        val config = HikariConfig().apply {
            jdbcUrl = instance<RepositoryConfiguration>().jdbcUrl
            driverClassName = instance<RepositoryConfiguration>().dbDriverClassName
            username = instance<RepositoryConfiguration>().dbUsername
            password = instance<RepositoryConfiguration>().dbPassword
            maximumPoolSize = instance<RepositoryConfiguration>().dbMaximumPoolSize
        }
        HikariDataSource(config)
    }
    bind<DatabaseProvider>() with eagerSingleton { DatabaseProvider(instance()) }
}

class DatabaseProvider(private val dataSource: DataSource) {

    init {
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(Users) //, Matches)
            SchemaUtils.createMissingTablesAndColumns(Users) //, Matches)
        }
    }
}
