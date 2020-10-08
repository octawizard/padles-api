package com.octawizard.repository

import com.octawizard.domain.model.Match
import com.octawizard.domain.model.User
import com.octawizard.repository.match.CacheMatchRepository
import com.octawizard.repository.match.DatabaseMatchRepository
import com.octawizard.repository.match.MatchRepository
import com.octawizard.repository.user.UserRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.redisson.api.RedissonClient
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModuleTest {

    @Test
    fun `UseCaseModule should inject dependencies for use cases`() {
        val kodein = DI {
            import(repositoryModule)
        }

        assertNotNull(kodein.direct.instance<RedisCache<String, User>>(tag = "userCache"))
        assertNotNull(kodein.direct.instance<RedisCache<UUID, Match>>(tag = "matchCache"))
        assertNotNull(kodein.direct.instance<RedissonClient>())
        assertNotNull(kodein.direct.instance<UserRepository>(tag = "db"))
        assertNotNull(kodein.direct.instance<UserRepository>())
        assertTrue(kodein.direct.instance<MatchRepository>(tag = "db") is DatabaseMatchRepository)
        assertTrue(kodein.direct.instance<MatchRepository>() is CacheMatchRepository)
    }
}
