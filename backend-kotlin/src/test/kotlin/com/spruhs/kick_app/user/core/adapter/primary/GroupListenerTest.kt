package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.EventExecutionStrategy
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.api.PlayerDowngradedEvent
import com.spruhs.kick_app.group.api.PlayerInvitedEvent
import com.spruhs.kick_app.group.api.PlayerPromotedEvent
import com.spruhs.kick_app.group.api.PlayerRemovedEvent
import com.spruhs.kick_app.user.core.application.MessageParams
import com.spruhs.kick_app.user.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.domain.MessageType
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GroupListenerTest {

    @MockK
    lateinit var messageUseCases: MessageUseCases

    lateinit var listener: GroupListener

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        listener = GroupListener(
            messageUseCases = messageUseCases,
            eventExecutionStrategy = object : EventExecutionStrategy {
                override fun execute(block: suspend () -> Unit) {
                    runBlocking { block() }
                }
            }
        )
    }

    @Test
    fun `onEvent should send message when player is invited`() = runBlocking {
        // Given
        val event = PlayerInvitedEvent(
            userId = UserId("user-123"),
            aggregateId = "aggregate-789",
            groupName = "Test User",
        )

        coEvery { messageUseCases.send(any(), any()) } returns Unit

        // When
        listener.onEvent(event)

        // Then
        coVerify {
            messageUseCases.send(MessageType.USER_INVITED_TO_GROUP, MessageParams(
                userId = event.userId,
                groupId = GroupId(event.aggregateId),
                groupName = event.groupName
            ))
        }
    }

    @Test
    fun `onEvent should handle player removed event`() = runBlocking {
        // Given
        val event = PlayerRemovedEvent("aggregate-123", UserId("user-456"), "Test User")

        coEvery { messageUseCases.send(any(), any()) } returns Unit

        // When
        listener.onEvent(event)

        // Then
        coVerify {
            messageUseCases.send(MessageType.USER_REMOVED_FROM_GROUP, MessageParams(
                userId = event.userId,
                groupId = GroupId(event.aggregateId),
                groupName = event.groupName
            ))
        }
    }

    @Test
    fun `onEvent should handle player promoted event`() = runBlocking {
        // Given
        val event = PlayerPromotedEvent(
            userId = UserId("user-123"),
            aggregateId = "aggregate-789",
        )

        coEvery { messageUseCases.send(any(), any()) } returns Unit

        // When
        listener.onEvent(event)

        // Then
        coVerify {
            messageUseCases.send(MessageType.USER_PROMOTED, MessageParams(
                userId = event.userId,
                groupId = GroupId(event.aggregateId),
            ))
        }
    }

    @Test
    fun `onEvent should handle player downgraded event`() = runBlocking {
        // Given
        val event = PlayerDowngradedEvent(
            userId = UserId("user-123"),
            aggregateId = "aggregate-789",
        )

        coEvery { messageUseCases.send(any(), any()) } returns Unit

        // When
        listener.onEvent(event)

        // Then
        coVerify {
            messageUseCases.send(MessageType.USER_DOWNGRADED, MessageParams(
                userId = event.userId,
                groupId = GroupId(event.aggregateId),
            ))
        }
    }
}