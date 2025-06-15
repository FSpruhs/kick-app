package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.application.UserCommandsPort
import com.spruhs.kick_app.user.core.application.UserQueryPort
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import com.spruhs.kick_app.TestHelpers.jwtWithUserId
import com.spruhs.kick_app.TestSecurityConfig
import com.spruhs.kick_app.user.TestUserBuilder
import com.spruhs.kick_app.user.core.application.ChangeUserNickNameCommand
import com.spruhs.kick_app.user.core.domain.NickName
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestSecurityConfig::class, JWTParser::class, UserRestControllerIT.TestConfig::class)
class UserRestControllerIT {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun userQueryPort(): UserQueryPort = mockk(relaxed = true)

        @Bean
        fun userCommandsPort(): UserCommandsPort = mockk(relaxed = true)
    }

    @Autowired
    lateinit var userQueryPort: UserQueryPort

    @Autowired
    lateinit var userCommandsPort: UserCommandsPort

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `getUser should get user`() {
        val userBuilder = TestUserBuilder()

        coEvery { userQueryPort.getUser(UserId(userBuilder.id)) } returns userBuilder.buildProjection()

        webTestClient.get()
            .uri("/api/v1/user/${userBuilder.id}")
            .header("Authorization", "Bearer ${jwtWithUserId(userBuilder.id)}")
            .exchange()
            .expectStatus().isOk
            .expectBody<UserMessage>()
            .consumeWith { response ->
                assertThat(response.responseBody).isNotNull()
                assertThat(response.responseBody).isEqualTo(userBuilder.buildMessage())
            }
    }

    @Test
    fun `getUser should throw exception when different user requested`() {
        val userBuilder = TestUserBuilder()

        webTestClient.get()
            .uri("/api/v1/user/${userBuilder.id}")
            .header("Authorization", "Bearer ${jwtWithUserId("differentId")}")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `registerUser should register user`() {
        val userBuilder = TestUserBuilder()

        coEvery { userCommandsPort.registerUser(any()) } returns userBuilder.buildAggregate()

        webTestClient.post()
            .uri("/api/v1/user")
            .header("Authorization", "Bearer ${jwtWithUserId(userBuilder.id)}")
            .bodyValue(userBuilder.buildRegisterUserRequest())
            .exchange()
            .expectStatus().isCreated
            .expectBody<UserMessage>()
            .consumeWith { response ->
                assertThat(response.responseBody).isNotNull()
                assertThat(response.responseBody).isEqualTo(userBuilder.buildMessage())
            }
    }

    @Test
    fun `changeNickName should change user nick name`() {
        val userBuilder = TestUserBuilder()
        val newNickname = NickName("newNickName")

        coEvery {
            userCommandsPort.changeNickName(
                ChangeUserNickNameCommand(
                    UserId(userBuilder.id),
                    newNickname
                )
            )
        } returns Unit

        webTestClient.put()
            .uri("/api/v1/user/${userBuilder.id}/nickName?nickName=${newNickname.value}")
            .header("Authorization", "Bearer ${jwtWithUserId(userBuilder.id)}")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `changeNickName should throw exception when different user requested`() {
        val userBuilder = TestUserBuilder()
        val newNickname = NickName("newNickName")

        webTestClient.put()
            .uri("/api/v1/user/${userBuilder.id}/nickName?nickName=${newNickname.value}")
            .header("Authorization", "Bearer ${jwtWithUserId("differentId")}")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
