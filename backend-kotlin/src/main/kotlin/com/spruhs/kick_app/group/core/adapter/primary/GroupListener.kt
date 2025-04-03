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

    @EventListener(PlayerEnteredGroupEvent::class)
    fun onEvent(event: PlayerEnteredGroupEvent) {
        applicationScope.launch {
            groupProjectionPort.whenEvent(event)
        }
    }

    @EventListener(PlayerPromotedEvent::class)
    fun onEvent(event: PlayerPromotedEvent) {
        applicationScope.launch {
            groupProjectionPort.whenEvent(event)
        }
    }

    @EventListener(PlayerDowngradedEvent::class)
    fun onEvent(event: PlayerDowngradedEvent) {
        applicationScope.launch {
            groupProjectionPort.whenEvent(event)
        }
    }

    @EventListener(PlayerActivatedEvent::class)
    fun onEvent(event: PlayerActivatedEvent) {
        applicationScope.launch {
            groupProjectionPort.whenEvent(event)
        }
    }

    @EventListener(PlayerDeactivatedEvent::class)
    fun onEvent(event: PlayerDeactivatedEvent) {
        applicationScope.launch {
            groupProjectionPort.whenEvent(event)
        }
    }

    @EventListener(PlayerRemovedEvent::class)
    fun onEvent(event: PlayerRemovedEvent) {
        applicationScope.launch {
            groupProjectionPort.whenEvent(event)
        }
    }

    @EventListener(PlayerLeavedEvent::class)
    fun onEvent(event: PlayerLeavedEvent) {
        applicationScope.launch {
            groupProjectionPort.whenEvent(event)
        }
    }
}