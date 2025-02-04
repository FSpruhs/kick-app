package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.group.api.UserInvitedToGroupEvent
import com.spruhs.kick_app.group.api.UserLeavedGroupEvent
import com.spruhs.kick_app.group.api.UserRemovedFromGroupEvent
import com.spruhs.kick_app.user.core.application.MessageParams
import com.spruhs.kick_app.user.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.domain.MessageType
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class GroupListener(val messageUseCases: MessageUseCases) {

    private val log = getLogger(this::class.java)

    @EventListener(UserInvitedToGroupEvent::class)
    fun onEvent(event: UserInvitedToGroupEvent) {
        log.info("UserInvitedToGroupEvent received: $event")
        messageUseCases.send(MessageType.USER_INVITED_TO_GROUP, event.toMessageParams())
    }

    @EventListener(UserLeavedGroupEvent::class)
    fun onEvent(event: UserLeavedGroupEvent) {
        log.info("UserLeavedGroupEvent received: $event")
        messageUseCases.send(MessageType.USER_LEAVED_GROUP, event.toMessageParams())
    }

    @EventListener(UserRemovedFromGroupEvent::class)
    fun onEvent(event: UserRemovedFromGroupEvent) {
        log.info("UserRemovedFromGroupEvent received: $event")
        messageUseCases.send(MessageType.USER_REMOVED_FROM_GROUP, event.toMessageParams())
    }
}

private fun UserInvitedToGroupEvent.toMessageParams() = MessageParams(
    userId = this.inviteeId,
    groupId = this.groupId,
    groupName = this.groupName
)

private fun UserLeavedGroupEvent.toMessageParams() = MessageParams(
    userId = this.userId,
    groupId = this.groupId,
    groupName = this.groupName
)

private fun UserRemovedFromGroupEvent.toMessageParams() = MessageParams(
    userId = this.userId,
    groupId = this.groupId,
    groupName = this.groupName
)