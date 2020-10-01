package com.octawizard.repository

import com.octawizard.repository.match.DatabaseMatchRepository
import com.octawizard.repository.match.MatchRepository
import com.octawizard.repository.user.DatabaseUserRepository
import com.octawizard.repository.user.UserRepository
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val repositoryModule = DI.Module("repository") {
    bind<UserRepository>() with singleton { DatabaseUserRepository() }
    bind<MatchRepository>() with singleton { DatabaseMatchRepository() }
}
