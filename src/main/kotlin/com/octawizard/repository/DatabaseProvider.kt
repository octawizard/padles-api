package com.octawizard.repository

import com.octawizard.repository.user.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

class DatabaseProvider(dataSource: DataSource) {

    init {
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(Users)
            SchemaUtils.createMissingTablesAndColumns(Users)
        }
    }
}
