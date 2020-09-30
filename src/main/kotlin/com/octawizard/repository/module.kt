package com.octawizard.repository

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val repositoryModule = DI.Module("repository") {
//    bind<UserRepository>() with singleton { InMemoryUserRepository() }
    bind<UserRepository>() with singleton { DatabaseUserRepository() }
    bind<MatchRepository>() with singleton { InMemoryMatchRepository() }
}
