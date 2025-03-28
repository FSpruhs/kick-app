package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.api.UserEnteredGroupEvent
import com.spruhs.kick_app.group.api.UserInvitedToGroupEvent
import com.spruhs.kick_app.group.api.UserLeavedGroupEvent
import com.spruhs.kick_app.group.api.UserRemovedFromGroupEvent
import com.spruhs.kick_app.user.core.application.MessageParams
import com.spruhs.kick_app.user.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.application.UserUseCases
import com.spruhs.kick_app.user.core.domain.MessageType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GroupListenerTest {

    @MockK
    lateinit var messageUseCases: MessageUseCases

    @MockK
    lateinit var userUseCases: UserUseCases

    @InjectMockKs
    lateinit var groupListener: GroupListener

    @Test
    fun `onEvent should send message when UserInvitedToGroupEvent is received`() = runBlocking {
        // given
        val event = UserInvitedToGroupEvent("inviteeId", "groupId", "groupName")
        coEvery { messageUseCases.send(any(), any()) } returns Unit

        // when
        groupListener.onEvent(event)

        // then
        coVerify {
            messageUseCases.send(
                MessageType.USER_INVITED_TO_GROUP,
                MessageParams(userId = event.inviteeId, groupId = event.groupId, groupName = event.groupName)
            )
        }
    }

    @Test
    fun `onEvent should send message when UserLeavedGroupEvent is received`() = runBlocking {
        // given
        val event = UserLeavedGroupEvent("userId", "groupId", "groupName")
        coEvery { messageUseCases.send(any(), any()) } returns Unit
        coEvery { userUseCases.userLeavesGroup(UserId(event.userId), GroupId(event.groupId)) } returns Unit


        // when
        groupListener.onEvent(event)

        // then
        coVerify {
            messageUseCases.send(
                MessageType.USER_LEAVED_GROUP,
                MessageParams(userId = event.userId, groupId = event.groupId, groupName = event.groupName)
            )
        }

        coVerify { userUseCases.userLeavesGroup(UserId(event.userId), GroupId(event.groupId)) }
    }

    @Test
    fun `onEvent should send message when UserRemovedFromGroupEvent is received`() = runBlocking {
        // given
        val event = UserRemovedFromGroupEvent("userId", "groupId", "groupName")
        coEvery { messageUseCases.send(any(), any()) } returns Unit
        coEvery { userUseCases.userLeavesGroup(UserId(event.userId), GroupId(event.groupId)) } returns Unit

        // when
        groupListener.onEvent(event)

        // then
        coVerify {
            messageUseCases.send(
                MessageType.USER_REMOVED_FROM_GROUP,
                MessageParams(userId = event.userId, groupId = event.groupId, groupName = event.groupName)
            )
        }

        coVerify { userUseCases.userLeavesGroup(UserId(event.userId), GroupId(event.groupId)) }
    }

    @Test
    fun `onEvent should call userEntersGroup when UserEnteredGroupEvent is received`() = runBlocking {
        // given
        val event = UserEnteredGroupEvent("userId", "groupId")
        coEvery { userUseCases.userEntersGroup(UserId(event.userId), GroupId(event.groupId)) } returns Unit

        // when
        groupListener.onEvent(event)

        // then
        coVerify { userUseCases.userEntersGroup(UserId(event.userId), GroupId(event.groupId)) }
    }

}