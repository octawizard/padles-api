package com.octawizard.repository

import com.mongodb.client.ClientSession
import com.mongodb.client.MongoClient
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MongoSessionProviderTest {

    @Test
    fun `MongoSessionProvider should return a client session`() {
        val clientSession = mockk<ClientSession>()
        val mongoClient = mockk<MongoClient>()
        every { mongoClient.startSession() } returns clientSession

        val mongoSessionProvider = MongoSessionProvider(mongoClient)

        assertEquals(clientSession, mongoSessionProvider.startClientSession())
    }
}
