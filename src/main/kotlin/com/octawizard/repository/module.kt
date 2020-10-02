package com.octawizard.repository

import com.octawizard.domain.model.User
import com.octawizard.repository.match.DatabaseMatchRepository
import com.octawizard.repository.match.MatchRepository
import com.octawizard.repository.user.CacheUserRepository
import com.octawizard.repository.user.DatabaseUserRepository
import com.octawizard.repository.user.UserRepository
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.redisson.api.RedissonClient
import java.util.concurrent.TimeUnit

val repositoryModule = DI.Module("repository") {
    bind<UserRepository>(tag = "db") with singleton { DatabaseUserRepository() }
    bind<UserRepository>() with
            singleton { CacheUserRepository(instance("userCache"), instance("db")) }
    bind<MatchRepository>() with singleton { DatabaseMatchRepository() }
    bind<RedissonClient>() with singleton { RedissonClientFactory.create("redis://127.0.0.1:6379") } //todo put in config
    bind<RedisCache<String, User>>(tag = "userCache") with
            singleton { RedisCache(instance(), "users", 24, TimeUnit.HOURS) } //todo put in config
}
