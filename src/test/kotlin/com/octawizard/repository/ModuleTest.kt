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
import org.junit.ClassRule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.singleton
import org.redisson.api.RedissonClient
import org.testcontainers.containers.GenericContainer
import java.time.Duration
import javax.sql.DataSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModuleTest {

    private val databaseConfig: DatabaseConfiguration
    private val redisConfig: RedisRepositoryConfiguration
    private val mongoConfig: MongoRepositoryConfiguration

    companion object {
        val db = "test-db"
        val dbUser= "test_user"
        val dbPassword = "test_password"

        @get:ClassRule
        @JvmStatic
        val redis: GenericContainer<Nothing> = object : GenericContainer<Nothing>("redis:6.0.8-alpine") {
            init {
                withExposedPorts(6379)
            }
        }

        @get:ClassRule
        @JvmStatic
        val mongo: GenericContainer<Nothing> = object : GenericContainer<Nothing>("mongo") {
            init {
                withExposedPorts(27017)
            }
        }

        @get:ClassRule
        @JvmStatic
        val postgres: GenericContainer<Nothing> = object : GenericContainer<Nothing>("postgres:13.0-alpine") {
            init {
                withExposedPorts(5432)
                withEnv("POSTGRES_DB", db)
                withEnv("POSTGRES_PASSWORD", dbPassword)
                withEnv("POSTGRES_USER", dbUser)
            }
        }
    }

    init {
        redis.start()
        val host = redis.host
        val port = redis.firstMappedPort
        redisConfig = RedisRepositoryConfiguration(
            "redis", host, port, Duration.ofMillis(500), Duration.ofSeconds(1), "users"
        )

        postgres.start()
        databaseConfig = DatabaseConfiguration(
            "jdbc:postgresql://localhost:${postgres.firstMappedPort}/$db",
            "org.postgresql.Driver",
            dbUser,
            dbPassword,
            4
        )

        mongo.start()
        mongoConfig = MongoRepositoryConfiguration(
            "localhost",
            mongo.firstMappedPort,
            "test",
            "clubs",
            "reservations",
        )
    }

    @AfterAll
    fun `shutdown Redis`() {
        redis.stop()
    }

    @Test
    fun `RepositoryModule should inject dependencies for repositories`() {
        val kodein = DI {
            bind<DatabaseConfiguration>() with singleton { databaseConfig }
            bind<RedisRepositoryConfiguration>() with singleton { redisConfig }
            bind<MongoRepositoryConfiguration>() with singleton { mongoConfig }
            import(repositoryModule)
        }

        assertNotNull(kodein.direct.instance<RedisCache<String, User>>(tag = userCache))
        assertNotNull(kodein.direct.instance<RedissonClient>())
        assertNotNull(kodein.direct.instance<UserRepository>(tag = database))
        assertNotNull(kodein.direct.instance<UserRepository>())
        assertNotNull(kodein.direct.instance<DatabaseProvider>())
        assertNotNull(kodein.direct.instance<DataSource>())
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
