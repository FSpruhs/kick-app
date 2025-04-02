package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.group.api.*
import com.spruhs.kick_app.group.core.domain.GroupProjectionPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class GroupListener (
    private val groupProjectionPort: GroupProjectionPort,
    private val applicationScope: CoroutineScope,
) {
    @EventListener(GroupCreatedEvent::class)
    fun onEvent(event: GroupCreatedEvent) {
        applicationScope.launch {
            groupProjectionPort.whenEvent(event)
        }
    }

    @EventListener(GroupNameChangedEvent::class)
    fun onEvent(event: GroupNameChangedEvent) {
        applicationScope.launch {
            groupProjectionPort.whenEvent(event)
        }
    }

    @EventListener(PlayerInvitedEvent::class)
    fun onEvent(event: PlayerInvitedEvent) {
        applicationScope.launch {
            groupProjectionPort.whenEvent(event)
        }
    }

    @EventListener(UserLeavedGroupEvent::class)
    fun onEvent(event: UserLeavedGroupEvent) {
        applicationScope.launch {
            groupProjectionPort.whenEvent(event)
        }
    }

    @EventListener(UserRemovedFromGroupEvent::class)
    fun onEvent(event: UserRemovedFromGroupEvent) {
        applicationScope.launch {
            groupProjectionPort.whenEvent(event)
        }
    }
}