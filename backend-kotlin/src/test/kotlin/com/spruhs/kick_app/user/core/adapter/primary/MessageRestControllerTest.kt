package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.TestHelpers.jwtWithUserId
import com.spruhs.kick_app.TestSecurityConfig
import com.spruhs.kick_app.common.helper.JWTParser
import com.spruhs.kick_app.common.types.MessageId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.user.core.application.MarkAsReadCommand
import com.spruhs.kick_app.user.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.application.UserCommandsPort
import com.spruhs.kick_app.user.core.domain.Message
import com.spruhs.kick_app.user.core.domain.MessageType
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestSecurityConfig::class, JWTParser::class, MessageRestControllerTest.TestConfig::class)
class MessageRestControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun messageUseCases(): MessageUseCases = mockk(relaxed = true)

        @Bean
        fun userCommandsPort(): UserCommandsPort = mockk(relaxed = true)
    }

    @Autowired
    lateinit var messageUseCases: MessageUseCases

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `getMessages should get messages`() {
        val userId = "testUserId"
        val message = Message(
            id = MessageId("messageId"),
            user = UserId(userId),
            text = "Hello",
            timeStamp = LocalDateTime.now(),
            type = MessageType.USER_REMOVED_FROM_GROUP,
            isRead = false,
            variables = emptyMap()
        )

        val messageResponse = MessageResponse(
            id = message.id.value,
            userId = message.user.value,
            text = message.text,
            timeStamp = message.timeStamp.toString(),
            type = message.type,
            isRead = message.isRead,
            variables = message.variables
        )

        coEvery { messageUseCases.getByUser(UserId(userId)) } returns listOf(message)

        webTestClient.get()
            .uri("/api/v1/message/user/$userId")
            .header("Authorization", "Bearer ${jwtWithUserId(userId)}")
            .exchange()
            .expectStatus().isOk
            .expectBody<List<MessageResponse>>()
            .consumeWith { response ->
                assertThat(response.responseBody).isNotNull()
                assertThat(response.responseBody).containsExactly(messageResponse)
            }
    }

    @Test
    fun `getMessages should throw exception when different user requested`() {
        webTestClient.get()
            .uri("/api/v1/message/user/testUserId")
            .header("Authorization", "Bearer ${jwtWithUserId("differentId")}")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `markMessageAsRead should mark message as read`() {
        val userId = "testUserId"
        val messageId = "messageId"

        coEvery { messageUseCases.markAsRead(MarkAsReadCommand(MessageId(messageId), UserId(userId))) } returns Unit

        webTestClient.put()
            .uri("/api/v1/message/$messageId/read")
            .header("Authorization", "Bearer ${jwtWithUserId(userId)}")
            .exchange()
            .expectStatus().isOk
    }
}