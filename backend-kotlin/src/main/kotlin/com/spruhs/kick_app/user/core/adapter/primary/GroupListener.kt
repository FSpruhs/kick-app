package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.group.api.*
import com.spruhs.kick_app.user.core.application.*
import com.spruhs.kick_app.user.core.domain.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component("UserGroupListener")
class GroupListener(
    private val messageUseCases: MessageUseCases,
    private val applicationScope: CoroutineScope
) {

    @EventListener
    fun onEvent(event: PlayerInvitedEvent) {
        applicationScope.launch {
            messageUseCases.send(MessageType.USER_INVITED_TO_GROUP, event.toMessageParams())
        }
    }

    @EventListener(PlayerRemovedEvent::class)
    fun onEvent(event: PlayerRemovedEvent) {
        applicationScope.launch {
            messageUseCases.send(MessageType.USER_REMOVED_FROM_GROUP, event.toMessageParams())
        }
    }

    @EventListener(PlayerPromotedEvent::class)
    fun onEvent(event: PlayerPromotedEvent) {
        applicationScope.launch {
            messageUseCases.send(MessageType.USER_PROMOTED, event.toMessageParams())
        }
    }

    @EventListener(PlayerDowngradedEvent::class)
    fun onEvent(event: PlayerDowngradedEvent) {
        applicationScope.launch {
            messageUseCases.send(MessageType.USER_DOWNGRADED, event.toMessageParams())
        }
    }
}

private fun PlayerDowngradedEvent.toMessageParams() = MessageParams(
    userId = this.userId,
    groupId = GroupId(this.aggregateId),
)

private fun PlayerPromotedEvent.toMessageParams() = MessageParams(
    userId = this.userId,
    groupId = GroupId(this.aggregateId),
)

private fun PlayerInvitedEvent.toMessageParams() = MessageParams(
    userId = this.userId,
    groupId = GroupId(this.aggregateId),
    groupName = this.groupName
)

private fun PlayerRemovedEvent.toMessageParams() = MessageParams(
    userId = this.userId,
    groupId = GroupId(this.aggregateId),
    groupName = this.groupName
)