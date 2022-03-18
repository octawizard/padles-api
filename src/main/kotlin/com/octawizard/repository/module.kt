package com.octawizard.repository

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.WriteConcern
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.octawizard.domain.model.User
import com.octawizard.repository.Tags.clubsCollection
import com.octawizard.repository.Tags.database
import com.octawizard.repository.Tags.reservationsCollection
import com.octawizard.repository.Tags.userCache
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.club.DocumentClubRepository
import com.octawizard.repository.club.model.ClubDTO
import com.octawizard.repository.reservation.DocumentReservationRepository
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.reservation.model.ClubReservationInfoDTO
import com.octawizard.repository.reservation.model.MatchDTO
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
import org.litote.kmongo.ascendingIndex
import org.litote.kmongo.div
import org.litote.kmongo.ensureIndex
import org.litote.kmongo.geo2dsphere
import org.litote.kmongo.getCollection
import org.litote.kmongo.textIndex
import org.redisson.api.RedissonClient
import javax.sql.DataSource

val repositoryModule = DI.Module("repository") {
    bind<RedisCache<String, User>>(tag = userCache) with singleton {
        val config = instance<RedisRepositoryConfiguration>()
        RedisCache.create(instance(), config.userCacheName, config.userCacheTtl)
    }
    bind<UserRepository>(tag = database) with singleton { DatabaseUserRepository() }
    bind<UserRepository>() with singleton { CacheUserRepository(instance(userCache), instance(database)) }

    bind<MongoDatabase>() with eagerSingleton {
        instance<MongoClient>().getDatabase(instance<MongoRepositoryConfiguration>().database)
    }
    bind<MongoSessionProvider>() with eagerSingleton { MongoSessionProvider(instance()) }
    bind<MongoCollection<ReservationDTO>>(tag = reservationsCollection) with singleton {
        val config = instance<MongoRepositoryConfiguration>()
        val reservations =
            instance<MongoDatabase>().getCollection<ReservationDTO>(config.reservationsCollectionName)
                .withWriteConcern(WriteConcern.MAJORITY)
        reservations.ensureIndexes(
            ascendingIndex(ReservationDTO::match / MatchDTO::players / User::email),
            geo2dsphere(ReservationDTO::clubReservationInfo / ClubReservationInfoDTO::location),
        )
        reservations
    }
    bind<MongoCollection<ClubDTO>>(tag = clubsCollection) with singleton {
        val config = instance<MongoRepositoryConfiguration>()
        val clubs =
            instance<MongoDatabase>().getCollection<ClubDTO>(config.clubsCollectionName)
                .withWriteConcern(WriteConcern.MAJORITY)

        clubs.ensureIndexes(
            ascendingIndex(ClubDTO::name),
            geo2dsphere(ClubDTO::geoLocation),
            ClubDTO::name.textIndex(),
        )
        clubs
    }
    bind<ReservationRepository>() with singleton {
        DocumentReservationRepository(instance(reservationsCollection))
    }
    bind<TransactionRepository>() with singleton {
        DocumentTransactionRepository(
            instance(clubsCollection),
            instance(reservationsCollection),
            instance(),
        )
    }
    bind<ClubRepository>() with singleton { DocumentClubRepository(instance(clubsCollection)) }
}

fun <T> MongoCollection<T>.ensureIndexes(vararg indexes: Bson) {
    indexes.forEach { ensureIndex(it) }
}

internal object Tags {
    const val clubsCollection = "clubsCollection"
    const val reservationsCollection = "reservationsCollection"
    const val userCache = "userCache"
    const val database = "database"
}

val dataSourcesModule = DI.Module("data-sources") {
    bind<RedissonClient>() with singleton {
        val config = instance<RedisRepositoryConfiguration>()
        val address = "${config.protocol}://${config.host}:${config.port}"
        RedissonClientFactory.create(address, config.timeout)
    }

    bind<DataSource>() with singleton {
        val config = HikariConfig().apply {
            jdbcUrl = instance<DatabaseConfiguration>().jdbcUrl
            driverClassName = instance<DatabaseConfiguration>().dbDriverClassName
            username = instance<DatabaseConfiguration>().dbUsername
            password = instance<DatabaseConfiguration>().dbPassword
            maximumPoolSize = instance<DatabaseConfiguration>().dbMaximumPoolSize
        }
        HikariDataSource(config)
    }

    bind<DatabaseProvider>() with eagerSingleton { DatabaseProvider(instance()) }

    bind<MongoClient>() with eagerSingleton {
        val settings = MongoClientSettings.builder()
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .applyConnectionString(ConnectionString(instance<MongoRepositoryConfiguration>().connectionString))
            .build()
        KMongo.createClient(settings)
    }
}
