package com.spruhs.kick_app

import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
abstract class AbstractMongoTest {

    companion object {

        private val mongoContainer = MongoDBContainer("mongo:7.0").apply {
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerMongoProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { mongoContainer.replicaSetUrl }
        }
    }

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @AfterEach
    fun cleanUpDatabase() {
        mongoTemplate.db.drop()
    }
}
