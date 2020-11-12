package com.octawizard.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import java.time.Duration

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigModuleTest {

    @Test
    fun `RepositoryConfigurationModule should inject DatabaseConfiguration`() {
        val kodein = DI {
            import(repositoryConfigurationModule)
        }
        val instance = kodein.direct.instance<DatabaseConfiguration>()
        assertNotNull(instance)
        assertEquals("jdbc:postgresql://localhost:5432/testdb", instance.jdbcUrl)
        assertEquals("org.postgresql.Driver", instance.dbDriverClassName)
        assertEquals("test_user", instance.dbUsername)
        assertEquals("test_password", instance.dbPassword)
        assertEquals(1, instance.dbMaximumPoolSize)
    }

    @Test
    fun `RepositoryConfigurationModule should inject RedisRepositoryConfiguration`() {
        val kodein = DI {
            import(repositoryConfigurationModule)
        }
        val instance = kodein.direct.instance<RedisRepositoryConfiguration>()
        assertNotNull(instance)
        assertEquals("test", instance.protocol)
        assertEquals("127.0.0.1", instance.host)
        assertEquals(1234, instance.port)
        assertEquals(Duration.ofSeconds(1), instance.timeout)
        assertEquals(Duration.ofHours(1), instance.userCacheTtl)
        assertEquals("users", instance.userCacheName)
    }

    @Test
    fun `RepositoryConfigurationModule should inject MongoRepositoryConfiguration`() {
        val kodein = DI {
            import(repositoryConfigurationModule)
        }
        val instance = kodein.direct.instance<MongoRepositoryConfiguration>()
        assertNotNull(instance)
        assertEquals("test-db", instance.database)
        assertEquals("localhost", instance.host)
        assertEquals(27017, instance.port)
        assertEquals("mongodb://localhost:27017", instance.connectionString)
        assertEquals("clubs", instance.clubsCollectionName)
        assertEquals("reservations", instance.reservationsCollectionName)
    }
}
