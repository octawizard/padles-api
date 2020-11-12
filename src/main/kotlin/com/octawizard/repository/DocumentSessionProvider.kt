package com.octawizard.repository

import com.mongodb.client.ClientSession
import com.mongodb.client.MongoClient

class DocumentSessionProvider(private val client: MongoClient) {

    fun startClientSession(): ClientSession {
        return client.startSession()
    }
}
