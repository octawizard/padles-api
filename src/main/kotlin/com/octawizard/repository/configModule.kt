package com.octawizard.repository

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val repositoryConfigurationModule = DI.Module("repositoryConfig") {
    bind<DatabaseConfiguration>() with singleton { DatabaseConfigurationFactory.build() }
    bind<RedisRepositoryConfiguration>() with singleton { RedisRepositoryConfigurationFactory.build() }
    bind<MongoRepositoryConfiguration>() with singleton { MongoRepositoryConfigurationFactory.build() }
}
