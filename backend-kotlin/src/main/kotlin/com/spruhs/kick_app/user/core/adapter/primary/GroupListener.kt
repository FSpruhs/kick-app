package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.group.api.UserInvitedToGroupEvent
import com.spruhs.kick_app.user.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.domain.MessageVariables
import com.spruhs.kick_app.user.core.domain.UserInvitedToGroupMessage
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class GroupListener(val messageUseCases: MessageUseCases) {

    private val log = getLogger(this::class.java)

    @EventListener(UserInvitedToGroupEvent::class)
    fun onEvent(event: UserInvitedToGroupEvent) {
        log.info("UserInvitedToGroupEvent received: $event")
        messageUseCases.send(UserInvitedToGroupMessage::class, event.toMessageParams())
    }
}

private fun UserInvitedToGroupEvent.toMessageParams() = mapOf(
    MessageVariables.USER_ID to this.inviteeId,
    MessageVariables.GROUP_ID to this.groupId,
    MessageVariables.GROUP_NAME to this.groupName
)