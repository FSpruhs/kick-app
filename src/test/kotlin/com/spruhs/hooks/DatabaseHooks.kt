package com.spruhs.hooks

import io.cucumber.java.Before
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.jdbc.core.JdbcTemplate

class DatabaseHooks(
    private val jdbcTemplate: JdbcTemplate,
    private val mongoTemplate: MongoTemplate
) {
    @Before
    fun clearDatabases() {
        clearPostgres()
        clearMongo()
    }

    private fun clearPostgres() {
        listOf("events", "snapshots").forEach { table ->
            val schema = jdbcTemplate.queryForObject(
                "SELECT table_schema FROM information_schema.tables WHERE table_name = ? LIMIT 1",
                String::class.java,
                table
            )
            if (schema != null) {
                jdbcTemplate.execute("TRUNCATE TABLE \"$schema\".\"$table\" RESTART IDENTITY CASCADE")
            }
        }
    }

    private fun clearMongo() {
        mongoTemplate.db.listCollectionNames().forEach { collection ->
            mongoTemplate.dropCollection(collection)
        }
    }
}
