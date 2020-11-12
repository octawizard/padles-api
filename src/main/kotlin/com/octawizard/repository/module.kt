package com.octawizard.repository

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.WriteConcern
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Indexes
import com.octawizard.domain.model.User
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.club.DocumentClubRepository
import com.octawizard.repository.club.model.ClubDTO
import com.octawizard.repository.reservation.DocumentReservationRepository
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.reservation.model.ReservationDTO
import com.octawizard.repository.transaction.DocumentTransactionRepository
import com.octawizard.repository.transaction.TransactionRepository
import com.octawizard.repository.user.CacheUserRepository
import com.octawizard.repository.user.DatabaseUserRepository
import com.octawizard.repository.user.UserRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bson.UuidRepresentation
import org.bson.conversions.Bson
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.eagerSingleton
import org.kodein.di.instance
import org.kodein.di.singleton
import org.litote.kmongo.KMongo
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.getCollection
import org.redisson.api.RedissonClient
import javax.sql.DataSource

val repositoryModule = DI.Module("repository") {
    bind<RedisCache<String, User>>(tag = "userCache") with
            singleton { RedisCache(instance(), "users", instance<RepositoryConfiguration>().userCacheTtl) }
    bind<UserRepository>(tag = "db") with singleton { DatabaseUserRepository() }
    bind<UserRepository>() with
            singleton { CacheUserRepository(instance("userCache"), instance("db")) }
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

    bind<MongoClient>() with eagerSingleton {
        val settings = MongoClientSettings.builder()
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .applyConnectionString(ConnectionString("mongodb://localhost:27017"))
            .build()
        KMongo.createClient(settings)
    }
    bind<MongoDatabase>() with eagerSingleton {
        instance<MongoClient>().getDatabase("padles") //normal java driver usage, todo use config
    }
    bind<DocumentSessionProvider>() with eagerSingleton { DocumentSessionProvider(instance()) }
    bind<MongoCollection<ReservationDTO>>(tag = "reservationsCollection") with singleton {
        val reservations = instance<MongoDatabase>().getCollection<ReservationDTO>("reservations")
            .withWriteConcern(WriteConcern.MAJORITY)
        reservations.ensureIndexes(
            Indexes.ascending("matches.players.email.value"),
            Indexes.geo2dsphere("clubReservationInfo.location"),
        )
        reservations
    }
    bind<MongoCollection<ClubDTO>>(tag = "clubsCollection") with singleton {
        val clubs = instance<MongoDatabase>().getCollection<ClubDTO>("clubs")
            .withWriteConcern(WriteConcern.MAJORITY)
        clubs.ensureIndexes(
            Indexes.ascending("name"),
            Indexes.geo2dsphere("geoLocation"),
        )
        clubs
    }
    bind<ReservationRepository>() with singleton {
        DocumentReservationRepository(instance("reservationsCollection"))
    }
    bind<TransactionRepository>() with singleton {
        DocumentTransactionRepository(
            instance("clubsCollection"),
            instance("reservationsCollection"),
            instance(),
        )
    }
    bind<ClubRepository>() with singleton { DocumentClubRepository(instance("clubsCollection")) }
}

fun <T> MongoCollection<T>.ensureIndexes(vararg indexes: Bson) {
    indexes.forEach { ensureIndex(it) }
}
