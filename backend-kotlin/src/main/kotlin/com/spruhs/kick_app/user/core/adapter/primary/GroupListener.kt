package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.group.api.*
import com.spruhs.kick_app.user.core.application.*
import com.spruhs.kick_app.user.core.domain.MessageType
import com.spruhs.kick_app.user.core.domain.UserProjectionPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component("GroupGroupListener")
class GroupListener(
    private val messageUseCases: MessageUseCases,
    private val userProjectionPort: UserProjectionPort,
    private val applicationScope: CoroutineScope
) {

    private val log = getLogger(this::class.java)

    @EventListener(GroupNameChangedEvent::class)
    fun onEvent(event: GroupNameChangedEvent) {
        log.info("GroupNameChangedEvent received: $event")
        applicationScope.launch {
            userProjectionPort.whenEvent(event)
        }
    }

    @EventListener(GroupCreatedEvent::class)
    fun onEvent(event: GroupCreatedEvent) {
        log.info("GroupCreatedEvent received: $event")
        applicationScope.launch {
            userProjectionPort.whenEvent(event)
        }
    }

    @EventListener
    fun onEvent(event: PlayerInvitedEvent) {
        log.info("PlayerInvitedEvent received: $event")
        applicationScope.launch {
            messageUseCases.send(MessageType.USER_INVITED_TO_GROUP, event.toMessageParams())
        }
    }

    @EventListener(PlayerEnteredGroupEvent::class)
    fun onEvent(event: PlayerEnteredGroupEvent) {
        log.info("PlayerEnteredGroupEvent received: $event")
        applicationScope.launch {
            userProjectionPort.whenEvent(event)
        }
    }

    @EventListener(PlayerRemovedEvent::class)
    fun onEvent(event: PlayerRemovedEvent) {
        log.info("PlayerRemovedEvent received: $event")
        applicationScope.launch {
            userProjectionPort.whenEvent(event)
            messageUseCases.send(MessageType.USER_REMOVED_FROM_GROUP, event.toMessageParams())
        }
    }

    @EventListener(PlayerLeavedEvent::class)
    fun onEvent(event: PlayerLeavedEvent) {
        log.info("PlayerLeavedEvent received: $event")
        applicationScope.launch {
            userProjectionPort.whenEvent(event)
        }
    }
}

private fun PlayerInvitedEvent.toMessageParams() = MessageParams(
    userId = this.userId,
    groupId = this.aggregateId,
    groupName = this.name
)

private fun PlayerRemovedEvent.toMessageParams() = MessageParams(
    userId = this.userId,
    groupId = this.aggregateId,
    groupName = this.name
)