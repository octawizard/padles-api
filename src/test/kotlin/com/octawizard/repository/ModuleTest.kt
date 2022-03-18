package com.octawizard.repository

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.octawizard.domain.model.User
import com.octawizard.repository.Tags.clubsCollection
import com.octawizard.repository.Tags.database
import com.octawizard.repository.Tags.reservationsCollection
import com.octawizard.repository.Tags.userCache
import com.octawizard.repository.club.ClubRepository
import com.octawizard.repository.club.model.ClubDTO
import com.octawizard.repository.reservation.ReservationRepository
import com.octawizard.repository.reservation.model.ReservationDTO
import com.octawizard.repository.transaction.TransactionRepository
import com.octawizard.repository.user.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.singleton
import org.redisson.api.RMapCache
import org.redisson.api.RedissonClient
import java.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModuleTest {

    private val databaseConfig = DatabaseConfiguration(
        "jdbc:postgresql://localhost:6543/$db",
        "org.postgresql.Driver",
        dbUser,
        dbPassword,
        4
    )
    private val redisConfig: RedisRepositoryConfiguration = RedisRepositoryConfiguration(
        "redis", "host", 1234, Duration.ofMillis(500), Duration.ofSeconds(1), "users"
    )
    private val mongoConfig = MongoRepositoryConfiguration(
        "localhost",
        1111,
        "test",
        "clubs",
        "reservations",
    )

    companion object {
        const val db = "test-db"
        const val dbUser = "test_user"
        const val dbPassword = "test_password"
    }


    @Test
    fun `RepositoryModule should inject dependencies for repositories`() {
        val redissonClient = mockk<RedissonClient>()
        every { redissonClient.getMapCache<String, User>(any()) } returns mockk<RMapCache<String, User>>()
        val mongoClient = mockk<MongoClient>()
        val mongoDB = mockk<MongoDatabase>(relaxed = true)
        every { mongoClient.getDatabase(any()) } returns mongoDB

        val kodein = DI {
            bind<DatabaseConfiguration>() with singleton { databaseConfig }
            bind<RedisRepositoryConfiguration>() with singleton { redisConfig }
            bind<MongoRepositoryConfiguration>() with singleton { mongoConfig }
            bind<RedissonClient>() with singleton { redissonClient }
            bind<MongoClient>() with singleton { mongoClient }
            import(repositoryModule)
        }

        assertNotNull(kodein.direct.instance<RedisCache<String, User>>(tag = userCache))
        assertNotNull(kodein.direct.instance<RedissonClient>())
        assertNotNull(kodein.direct.instance<UserRepository>(tag = database))
        assertNotNull(kodein.direct.instance<UserRepository>())
        assertNotNull(kodein.direct.instance<MongoClient>())
        assertNotNull(kodein.direct.instance<MongoDatabase>())
        assertNotNull(kodein.direct.instance<MongoSessionProvider>())
        assertNotNull(kodein.direct.instance<MongoCollection<ClubDTO>>(tag = clubsCollection))
        assertNotNull(kodein.direct.instance<MongoCollection<ReservationDTO>>(tag = reservationsCollection))
        assertNotNull(kodein.direct.instance<ReservationRepository>())
        assertNotNull(kodein.direct.instance<ClubRepository>())
        assertNotNull(kodein.direct.instance<TransactionRepository>())
    }
}
