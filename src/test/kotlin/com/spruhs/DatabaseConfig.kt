package com.spruhs
import com.mongodb.client.MongoClient
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.jdbc.core.JdbcTemplate
@Configuration
@EnableMongoRepositories
@EntityScan
class DatabaseConfig
