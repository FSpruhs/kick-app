package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.MessageNotFoundException
import com.spruhs.kick_app.group.api.GroupApi
import com.spruhs.kick_app.user.core.TestMessageBuilder
import com.spruhs.kick_app.user.core.domain.MessagePersistencePort
import com.spruhs.kick_app.user.core.domain.MessageType
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class MessageUseCasesTest {

    @MockK
    private lateinit var messagePersistencePort: MessagePersistencePort

    @MockK
    private lateinit var groupApi: GroupApi

    @InjectMockKs
    private lateinit var messageUseCases: MessageUseCases

    @Test
    fun `send should save message to persistence`() {
        // given
        val message = TestMessageBuilder().build()
        every { messagePersistencePort.save(any()) } returns Unit

        // when
        messageUseCases.send(
            message.type, MessageParams(
                userId = message.user.value,
                groupId = "group id",
                groupName = "group name",
            )
        )

        // then
        verify { messagePersistencePort.save(any()) }
    }

    @Test
    fun `get by user should get by user`() {
        // given
        val message = TestMessageBuilder().build()
        every { messagePersistencePort.findByUser(message.user) } returns listOf(message)

        // when
        messageUseCases.getByUser(message.user).let { result ->
            // then
            assertThat(result).hasSize(1)
            assertThat(result.first()).isEqualTo(message)
        }
    }

    @Test
    fun `mark as read should mark message as read`() {
        // given
        val message = TestMessageBuilder().build()
        val command = MarkAsReadCommand(message.id, message.user)
        every { messagePersistencePort.findById(message.id) } returns message
        every { messagePersistencePort.save(any()) } returns Unit

        // when
        messageUseCases.markAsRead(command)

        // then
        verify { messagePersistencePort.save(any()) }
    }

    @Test
    fun `mark as read should throw exception if message not found`() {
        // given
        val message = TestMessageBuilder().build()
        val command = MarkAsReadCommand(message.id, message.user)
        every { messagePersistencePort.findById(command.messageId) } returns null

        // when
        // then
        assertThatThrownBy { messageUseCases.markAsRead(command) }
            .isInstanceOf(MessageNotFoundException::class.java)
    }

    @Test
    fun `send all active users in group message should save all messages`() {
        // given
        val groupId = GroupId("group id")
        val messages = listOf(
            TestMessageBuilder()
                .withId("1")
                .withType(MessageType.MATCH_CREATED)
                .withUserId("user id 1")
                .build(),
            TestMessageBuilder()
                .withId("2")
                .withType(MessageType.MATCH_CREATED)
                .withUserId("user id 2")
                .build()
        )
        every { groupApi.getActivePlayers(groupId) } returns messages.map { it.user }
        every { messagePersistencePort.saveAll(any()) } returns Unit

        // when
        messageUseCases.sendAllActiveUsersInGroupMessage(
            messages.first().type,
            MessageParams(start = LocalDateTime.now(), matchId = "match id", groupId = groupId.value),
            groupId
        )

        // then
        verify { messagePersistencePort.saveAll(any()) }
    }
}