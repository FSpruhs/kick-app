package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.api.PlayerDowngradedEvent
import com.spruhs.kick_app.group.api.PlayerInvitedEvent
import com.spruhs.kick_app.group.api.PlayerPromotedEvent
import com.spruhs.kick_app.group.api.PlayerRemovedEvent
import com.spruhs.kick_app.user.core.application.MessageParams
import com.spruhs.kick_app.user.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.domain.MessageType
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GroupListenerTest {

    @MockK
    lateinit var messageUseCases: MessageUseCases

    private val testScope = CoroutineScope(Dispatchers.Default)

    lateinit var listener: GroupListener


    @Test
    fun `onEvent should send message when player is invited`() = runBlocking {
        // Given
        listener = GroupListener(
            messageUseCases = messageUseCases,
            applicationScope = testScope
        )
        val event = PlayerInvitedEvent(
            userId = UserId("user-123"),
            aggregateId = "aggregate-789",
            name = "Test User",
        )

        // When
        listener.onEvent(event)

        // Then
        coVerify {
            messageUseCases.send(MessageType.USER_INVITED_TO_GROUP, MessageParams(
                userId = event.userId,
                groupId = GroupId(event.aggregateId),
                groupName = event.name
            ))
        }
    }

    @Test
    fun `onEvent should handle player removed event`() {
        // Given
        listener = GroupListener(
            messageUseCases = messageUseCases,
            applicationScope = testScope
        )
        val event = PlayerRemovedEvent("aggregate-123", UserId("user-456"), "Test User")

        // When
        listener.onEvent(event)

        // Then
        coVerify {
            messageUseCases.send(MessageType.USER_REMOVED_FROM_GROUP, MessageParams(
                userId = event.userId,
                groupId = GroupId(event.aggregateId),
                groupName = event.name
            ))
        }
    }

    @Test
    fun `onEvent should handle player promoted event`() {
        // Given
        listener = GroupListener(
            messageUseCases = messageUseCases,
            applicationScope = testScope
        )
        val event = PlayerPromotedEvent(
            userId = UserId("user-123"),
            aggregateId = "aggregate-789",
        )

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
    fun `onEvent should handle player downgraded event`() {
        // Given
        listener = GroupListener(
            messageUseCases = messageUseCases,
            applicationScope = testScope
        )
        val event = PlayerDowngradedEvent(
            userId = UserId("user-123"),
            aggregateId = "aggregate-789",
        )

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