package com.octawizard.mongo

import com.mongodb.ConnectionString
import com.mongodb.client.MongoClient
import com.octawizard.repository.host
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.IMongodConfig
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import org.bson.BsonArray
import org.bson.BsonDocument
import org.bson.BsonInt32
import org.bson.BsonString
import org.litote.kmongo.service.MongoClientProvider

internal object ReplicaSetEmbeddedMongo {

    private const val replicaSet = "kmongo"

    var ports = Network.getFreeServerPorts(Network.getLocalHost(), 3)

    val rep1: IMongodConfig = MongodConfigBuilder()
        .version(Version.Main.PRODUCTION)
        .net(Net(ports[0], Network.localhostIsIPv6()))
        .withLaunchArgument("--replSet", replicaSet)
        .cmdOptions(
            MongoCmdOptionsBuilder()
                .useSmallFiles(true)
                .useNoJournal(false)
                .build()
        )
        .build()
    val rep2: IMongodConfig = MongodConfigBuilder()
        .version(Version.Main.PRODUCTION)
        .net(Net(ports[1], Network.localhostIsIPv6()))
        .withLaunchArgument("--replSet", replicaSet)
        .cmdOptions(
            MongoCmdOptionsBuilder()
                .useSmallFiles(true)
                .useNoJournal(false)
                .build()
        )
        .build()
    val rep3: IMongodConfig = MongodConfigBuilder()
        .version(Version.Main.PRODUCTION)
        .net(Net(ports[2], Network.localhostIsIPv6()))
        .withLaunchArgument("--replSet", replicaSet)
        .cmdOptions(
            MongoCmdOptionsBuilder()
                .useSmallFiles(true)
                .useNoJournal(false)
                .build()
        )
        .build()


    private val mongodProcesses: List<MongodProcess> by lazy { createInstances() }

    fun connectionString(): ConnectionString {
        initiateReplicaSet()
        return mongodProcesses.run {
            ConnectionString(
                "mongodb://${first().host},${get(1).host},${get(2).host}/?replicaSet=$replicaSet"
            )
        }
    }

    private fun initiateReplicaSet() {
        val host = mongodProcesses[0].host
        val conf = BsonDocument("_id", BsonString(replicaSet))
            .apply {
                put("protocolVersion", BsonInt32(1))
                put("version", BsonInt32(1))
                put(
                    "members",
                    BsonArray(
                        mongodProcesses.mapIndexed { i, p ->
                            val s = BsonDocument("_id", BsonInt32(i))
                            s.put("host", BsonString(p.host))
                            s
                        })
                )
            }
        val initCommand = BsonDocument("replSetInitiate", conf)

        MongoClientProvider
            .createMongoClient<MongoClient>(ConnectionString("mongodb://$host"))
            .getDatabase("admin")
            .runCommand(initCommand)
    }

    private fun createInstances(): List<MongodProcess> =
        listOf(
            MongodStarter.getInstance(EmbeddedMongoLog.embeddedConfig).prepare(rep1).start(),
            MongodStarter.getInstance(EmbeddedMongoLog.embeddedConfig).prepare(rep2).start(),
            MongodStarter.getInstance(EmbeddedMongoLog.embeddedConfig).prepare(rep3).start()
        )

}
