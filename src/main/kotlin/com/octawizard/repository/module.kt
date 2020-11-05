package com.octawizard.repository

//import com.octawizard.repository.match.CacheMatchRepository
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Indexes
import com.octawizard.domain.model.Match
import com.octawizard.domain.model.User
import com.octawizard.repository.reservation.DocumentReservationRepository
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.reservation.model.ReservationDTO
import com.octawizard.repository.user.CacheUserRepository
import com.octawizard.repository.user.DatabaseUserRepository
import com.octawizard.repository.user.UserRepository
import com.octawizard.repository.user.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bson.UuidRepresentation
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.eagerSingleton
import org.kodein.di.instance
import org.kodein.di.singleton
import org.litote.kmongo.KMongo
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.getCollection
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
//    bind<MatchRepository>(tag = "db") with singleton { DatabaseMatchRepository() }
//    bind<MatchRepository>() with singleton { CacheMatchRepository(instance("matchCache"), instance("db")) }
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

    bind<MongoDatabase>() with eagerSingleton {
        val settings = MongoClientSettings.builder()
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .applyConnectionString(ConnectionString("mongodb://localhost:27017"))
            .build()
        val client = KMongo.createClient(settings)
        client.getDatabase("padles") //normal java driver usage, todo use config
    }
    bind<ReservationRepository>() with singleton {
        val reservations = instance<MongoDatabase>().getCollection<ReservationDTO>("reservations")
        reservations.ensureIndexes()
        DocumentReservationRepository(reservations)
    }
}

fun MongoCollection<ReservationDTO>.ensureIndexes() {
    // index on match players
    ensureIndex(Indexes.ascending("matches.players.email.value"))
    // geo spatial index
    ensureIndex(Indexes.geo2dsphere("clubReservationInfo.location"))
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
