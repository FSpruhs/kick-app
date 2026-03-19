package com.spruhs.kick_app

import com.spruhs.kick_app.common.es.AggregateStore
import com.spruhs.kick_app.common.types.Email
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.user.core.domain.NickName
import com.spruhs.kick_app.user.core.domain.Password
import com.spruhs.kick_app.user.core.domain.UserIdentityProviderPort
import io.minio.MinioClient
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Import(MongoTestConfig::class)
abstract class AbstractMongoTest {
    companion object {
        private val mongoContainer =
            MongoDBContainer("mongo:7.0").apply {
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

    @MockitoBean
    lateinit var aggregateStore: AggregateStore

    @MockitoBean
    lateinit var minioClient: MinioClient

    @AfterEach
    fun cleanUpDatabase() {
        mongoTemplate.db.drop()
    }
}

@TestConfiguration
class MongoTestConfig {
    @Bean
    fun fakeUserIdentityProvider(): UserIdentityProviderPort =
        object : UserIdentityProviderPort {
            override suspend fun save(
                email: Email,
                nickName: NickName,
                password: Password?,
                userId: UserId?,
            ): UserId = UserId("fake-user-id")

            override suspend fun changeNickName(
                userId: UserId,
                nickName: NickName,
            ) {
                println()
            }
        }
}
