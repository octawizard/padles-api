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
    fun `RepositoryConfigurationModule should inject dependencies`() {
        val kodein = DI {
            import(repositoryConfigurationModule)
        }
        val instance = kodein.direct.instance<RepositoryConfiguration>()
        assertNotNull(instance)
        assertEquals("test", instance.protocol)
        assertEquals("127.0.0.1", instance.host)
        assertEquals(1234, instance.port)
        assertEquals(Duration.ofSeconds(1), instance.timeout)
        assertEquals(Duration.ofHours(1), instance.userCacheTtl)
        assertEquals(Duration.ofHours(2), instance.matchCacheTtl)
    }
}