package com.octawizard.repository.mongo

import de.flapdoodle.embed.mongo.Command
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder
import de.flapdoodle.embed.process.config.io.ProcessOutput

internal object EmbeddedMongoLog {

    val embeddedConfig = RuntimeConfigBuilder()
        .defaults(Command.MongoD)
        .processOutput(
            if (System.getProperty("kmongo.flapdoddle.log") == "true") ProcessOutput.getDefaultInstance("mongod")
            else ProcessOutput.getDefaultInstanceSilent()
        )
        .build()
}
