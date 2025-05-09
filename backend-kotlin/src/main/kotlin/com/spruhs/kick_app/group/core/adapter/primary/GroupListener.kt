package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.group.api.*
import com.spruhs.kick_app.group.core.domain.GroupProjectionPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component("GroupGroupListener")
class GroupListener (
    private val groupProjectionPort: GroupProjectionPort,
    private val applicationScope: CoroutineScope,
) {
    private val log = getLogger(this::class.java)

    @EventListener(
        GroupCreatedEvent::class,
        GroupNameChangedEvent::class,
        PlayerEnteredGroupEvent::class,
        PlayerPromotedEvent::class,
        PlayerDowngradedEvent::class,
        PlayerActivatedEvent::class,
        PlayerDeactivatedEvent::class,
        PlayerRemovedEvent::class,
        PlayerLeavedEvent::class
    )
    fun onEvent(event: BaseEvent) {
        log.info("Group scope received: $event")
        applicationScope.launch {
            groupProjectionPort.whenEvent(event)
        }
    }
}