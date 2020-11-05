//package com.octawizard.repository
//
//import com.octawizard.domain.model.Match
//import com.octawizard.domain.model.User
//import com.octawizard.repository.match.CacheMatchRepository
//import com.octawizard.repository.match.DatabaseMatchRepository
//import com.octawizard.repository.match.MatchRepository
//import com.octawizard.repository.user.UserRepository
//import org.junit.ClassRule
//import org.junit.jupiter.api.AfterAll
//import org.junit.jupiter.api.Assertions.assertNotNull
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.BeforeAll
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.TestInstance
//import org.kodein.di.*
//import org.redisson.api.RedissonClient
//import org.testcontainers.containers.GenericContainer
//import java.time.Duration
//import java.util.*
//
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class ModuleTest {
//
//    private val testConfig: RepositoryConfiguration
//
//    companion object {
//        @get:ClassRule
//        @JvmStatic
//        val redis: GenericContainer<Nothing> = object : GenericContainer<Nothing>("redis:6.0.8-alpine") {
//            init {
//                withExposedPorts(6379)
//            }
//        }
//    }
//
//    init {
//        redis.start()
//        val host = redis.host
//        val port = redis.firstMappedPort
//        testConfig = RepositoryConfiguration(
//            "redis", host, port, Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofSeconds(1)
//        )
//    }
//
//    @AfterAll
//    fun `shutdown Redis`() {
//        redis.stop()
//    }
//
//    @Test
//    fun `RepositoryModule should inject dependencies for repositories`() {
//        val kodein = DI {
//            bind<RepositoryConfiguration>() with singleton { testConfig }
//            import(repositoryModule)
//        }
//
//        assertNotNull(kodein.direct.instance<RedisCache<String, User>>(tag = "userCache"))
//        assertNotNull(kodein.direct.instance<RedisCache<UUID, Match>>(tag = "matchCache"))
//        assertNotNull(kodein.direct.instance<RedissonClient>())
//        assertNotNull(kodein.direct.instance<UserRepository>(tag = "db"))
//        assertNotNull(kodein.direct.instance<UserRepository>())
//        assertTrue(kodein.direct.instance<MatchRepository>(tag = "db") is DatabaseMatchRepository)
//        assertTrue(kodein.direct.instance<MatchRepository>() is CacheMatchRepository)
//    }
//}
