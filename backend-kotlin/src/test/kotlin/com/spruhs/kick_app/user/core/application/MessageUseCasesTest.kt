package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.types.MessageId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.message.core.application.MarkAsReadCommand
import com.spruhs.kick_app.message.core.application.MessageParams
import com.spruhs.kick_app.message.core.application.MessageUseCases
import com.spruhs.kick_app.view.api.GroupApi
import com.spruhs.kick_app.user.core.TestMessageBuilder
import com.spruhs.kick_app.message.core.domain.Message
import com.spruhs.kick_app.message.core.domain.MessagePersistencePort
import com.spruhs.kick_app.message.core.domain.MessageType
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class MessageUseCasesTest {

    @MockK
    lateinit var messagePersistencePort: MessagePersistencePort

    @MockK
    lateinit var groupApi: GroupApi

    @InjectMockKs
    lateinit var messageUseCases: MessageUseCases

    @ParameterizedTest
    @MethodSource("sendData")
    fun `send should send message`(expectedMessage: Message): Unit = runBlocking {
        // Given
        val messageParams = MessageParams(
            userId = UserId("userId"),
            groupId = GroupId("groupId"),
            groupName = "groupName",
            playground = "playgroundName",
            matchId = MatchId("matchId")
        )

        coEvery { messagePersistencePort.save(any()) } just Runs

        // When
        messageUseCases.send(expectedMessage.type, messageParams)

        // Then
        coVerify {
            messagePersistencePort.save(
                match {
                    it.type == expectedMessage.type &&
                            it.isRead == expectedMessage.isRead &&
                            it.variables == expectedMessage.variables &&
                            it.user == expectedMessage.user
                }
            )
        }
    }

    @Test
    fun `getByUser should return messages for user`(): Unit = runBlocking {
        // Given
        val userId = UserId("userId")
        val expectedMessages = listOf(
            TestMessageBuilder().withUser(userId.value).buildMessage()
        )

        coEvery { messagePersistencePort.findByUser(userId) } returns expectedMessages

        // When
        val result = messageUseCases.getByUser(userId)

        // Then
        assertThat(result).isEqualTo(expectedMessages)
    }

    @Test
    fun `markAsRead should mark message as read`(): Unit = runBlocking {
        // Given
        val messageId = MessageId("messageId")
        val userId = UserId("userId")
        val message = TestMessageBuilder().withUser(userId.value).buildMessage()

        coEvery { messagePersistencePort.findById(messageId) } returns message
        coEvery { messagePersistencePort.save(any()) } just Runs

        // When
        messageUseCases.markAsRead(MarkAsReadCommand(messageId, userId))

        // Then
        coVerify {
            messagePersistencePort.save(
                match {
                    it.isRead && it.id == message.id
                }
            )
        }
    }

    companion object {
        @JvmStatic
        fun sendData() = listOf(
            TestMessageBuilder()
                .withType(MessageType.USER_INVITED_TO_GROUP)
                .withVariables(mapOf("groupId" to "groupId"))
                .buildMessage(),

            TestMessageBuilder()
                .withType(MessageType.PLAYER_ADDED_TO_CADRE)
                .withVariables(mapOf("matchId" to "matchId"))
                .buildMessage(),

            TestMessageBuilder()
                .withType(MessageType.PLAYGROUND_CHANGED)
                .withVariables(mapOf("groupId" to "groupId"))
                .buildMessage(),

            TestMessageBuilder()
                .withType(MessageType.MATCH_CANCELED)
                .withVariables(
                    mapOf(
                        "groupId" to "groupId",
                        "matchId" to "matchId",
                    )
                )
                .buildMessage(),

            TestMessageBuilder()
                .withType(MessageType.USER_PROMOTED)
                .withVariables(mapOf("groupId" to "groupId"))
                .buildMessage(),

            TestMessageBuilder()
                .withType(MessageType.USER_DOWNGRADED)
                .withVariables(mapOf("groupId" to "groupId"))
                .buildMessage(),

            TestMessageBuilder()
                .withType(MessageType.MATCH_CREATED)
                .withVariables(
                    mapOf(
                        "groupId" to "groupId",
                        "matchId" to "matchId",
                    )
                )
                .buildMessage(),

            TestMessageBuilder()
                .withType(MessageType.USER_REMOVED_FROM_GROUP)
                .withVariables(mapOf("groupId" to "groupId"))
                .buildMessage(),

            TestMessageBuilder()
                .withType(MessageType.USER_LEAVED_GROUP)
                .withVariables(mapOf("groupId" to "groupId"))
                .buildMessage(),

            TestMessageBuilder()
                .withType(MessageType.PLAYER_PLACED_ON_WAITING_BENCH)
                .withVariables(mapOf("matchId" to "matchId"))
                .buildMessage(),
        )
    }
}