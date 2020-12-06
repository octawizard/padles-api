package com.octawizard.repository

import com.mongodb.client.ClientSession
import com.mongodb.client.MongoClient

class MongoSessionProvider(private val client: MongoClient) {

    fun startClientSession(): ClientSession {
        return client.startSession()
    }
}
