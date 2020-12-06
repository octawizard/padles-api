package com.octawizard.repository.mongo

import com.mongodb.ConnectionString
import com.octawizard.repository.host
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.IMongodConfig
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network

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
