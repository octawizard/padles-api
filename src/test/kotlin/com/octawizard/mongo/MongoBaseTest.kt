package com.octawizard.repository

import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.octawizard.mongo.ReplicaSetEmbeddedMongo
import com.octawizard.mongo.StandaloneEmbeddedMongo
import de.flapdoodle.embed.mongo.MongodProcess
import org.bson.UuidRepresentation
import org.bson.types.ObjectId
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.litote.kmongo.KMongo
import org.litote.kmongo.util.KMongoUtil
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

abstract class MongoBaseTestWithUUIDRepr<T : Any>(standalone: Boolean = true) {

    @Suppress("LeakingThis")
    @Rule
    @JvmField
    val rule = MongoFlapdoodleRule(getDefaultCollectionClass(), standalone = standalone)

    val col by lazy { rule.col }

    val database by lazy { rule.database }

    val mongoClient by lazy { rule.mongoClient }

    inline fun <reified T : Any> getCollection(): MongoCollection<T> = rule.getCollection()

    inline fun <reified T : Any> dropCollection() = rule.dropCollection<T>()

    @Suppress("UNCHECKED_CAST")
    fun getDefaultCollectionClass(): KClass<T> =
        ((this::class.java.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>).kotlin

}

internal val MongodProcess.host get() = "127.0.0.1:${config.net().port}"

class MongoFlapdoodleRule<T : Any>(
    val defaultDocumentClass: KClass<T>,
    val generateRandomCollectionName: Boolean = false,
    val dbName: String = "test",
    val standalone: Boolean = true,
) : TestRule {

    companion object {
        inline fun <reified T : Any> rule(generateRandomCollectionName: Boolean = false): MongoFlapdoodleRule<T> =
            MongoFlapdoodleRule(T::class, generateRandomCollectionName)
    }

    private val connectionString = if (!standalone) {
        ReplicaSetEmbeddedMongo.connectionString()
    } else {
        StandaloneEmbeddedMongo.connectionString()
    }

    val mongoClient: MongoClient by lazy {
        val settings = MongoClientSettings.builder()
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .applyConnectionString(connectionString)
            .build()
        KMongo.createClient(settings)
    }

    val database: MongoDatabase by lazy {
        mongoClient.getDatabase(dbName)
    }

    inline fun <reified T : Any> getCollection(): MongoCollection<T> =
        database.getCollection(KMongoUtil.defaultCollectionName(T::class), T::class.java)

    fun <T : Any> getCollection(clazz: KClass<T>): MongoCollection<T> =
        getCollection(KMongoUtil.defaultCollectionName(clazz), clazz)

    fun <T : Any> getCollection(name: String, clazz: KClass<T>): MongoCollection<T> =
        database.getCollection(name, clazz.java)

    inline fun <reified T : Any> dropCollection() = dropCollection(KMongoUtil.defaultCollectionName(T::class))

    fun dropCollection(clazz: KClass<*>) = dropCollection(KMongoUtil.defaultCollectionName(clazz))

    fun dropCollection(collectionName: String) {
        database.getCollection(collectionName).drop()
    }

    val col: MongoCollection<T> by lazy {
        val name = if (generateRandomCollectionName) {
            ObjectId().toString()
        } else {
            KMongoUtil.defaultCollectionName(defaultDocumentClass)
        }

        getCollection(name, defaultDocumentClass)
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            override fun evaluate() {
                try {
                    base.evaluate()
                } finally {
                    col.drop()
                }
            }
        }
    }
}
