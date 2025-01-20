package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.group.api.UserInvitedToGroupEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class GroupListener {

    private val log = getLogger(this::class.java)

    @EventListener(UserInvitedToGroupEvent::class)
    fun onEvent(event: UserInvitedToGroupEvent) {
        log.info("User invited to group: $event")
    }

}