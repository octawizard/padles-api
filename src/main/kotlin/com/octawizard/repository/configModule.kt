package com.octawizard.repository

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val repositoryConfigurationModule = DI.Module("repositoryConfig") {
    bind<RepositoryConfiguration>() with singleton { RepositoryConfigurationFactory.build() }
}

