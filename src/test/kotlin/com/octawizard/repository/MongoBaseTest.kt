package com.octawizard.repository

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import de.flapdoodle.embed.mongo.Command
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.IMongodConfig
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.runtime.Network
import org.bson.BsonDocument
import org.bson.Document
import org.bson.UuidRepresentation
import org.bson.types.ObjectId
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.kodein.di.instance
import org.litote.kmongo.KMongo
import org.litote.kmongo.service.MongoClientProvider
import org.litote.kmongo.util.KMongoUtil
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

abstract class MongoBaseTestWithUUIDRepr<T : Any>() {

    @Suppress("LeakingThis")
    @Rule
    @JvmField
    val rule = MongoFlapdoodleRule(getDefaultCollectionClass())

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

internal object StandaloneEmbeddedMongo {

    var port = Network.getFreeServerPort()
    var config: IMongodConfig = MongodConfigBuilder()
        .version(Version.Main.PRODUCTION)
        .net(Net(port, Network.localhostIsIPv6()))
        .build()

    private val mongodProcess: MongodProcess by lazy { createInstance() }

    fun connectionString(): ConnectionString = ConnectionString("mongodb://${mongodProcess.host}")

    private fun createInstance(): MongodProcess =
        MongodStarter.getInstance(EmbeddedMongoLog.embeddedConfig).prepare(config).start()
}

internal object EmbeddedMongoLog {

    val embeddedConfig = RuntimeConfigBuilder()
        .defaults(Command.MongoD)
        .processOutput(
            if (System.getProperty("kmongo.flapdoddle.log") == "true") ProcessOutput.getDefaultInstance("mongod")
            else ProcessOutput.getDefaultInstanceSilent()
        )
        .build();
}

class MongoFlapdoodleRule<T : Any>(
    val defaultDocumentClass: KClass<T>,
    val generateRandomCollectionName: Boolean = false,
    val dbName: String = "test",
) : TestRule {

    companion object {
        inline fun <reified T : Any> rule(generateRandomCollectionName: Boolean = false): MongoFlapdoodleRule<T> =
            MongoFlapdoodleRule(T::class, generateRandomCollectionName)
    }

    private fun default(host: String, command: BsonDocument) = MongoClientProvider
        .createMongoClient<MongoClient>(ConnectionString("mongodb://$host/?uuidRepresentation=STANDARD"))
        .getDatabase("admin")
        .runCommand(command)

    val mongoClient: MongoClient by lazy {
        val settings = MongoClientSettings.builder()
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .applyConnectionString(StandaloneEmbeddedMongo.connectionString())
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
