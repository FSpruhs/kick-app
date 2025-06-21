package com.spruhs.kick_app.view.core.controller.listener

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.group.api.GroupCreatedEvent
import com.spruhs.kick_app.group.api.GroupNameChangedEvent
import com.spruhs.kick_app.group.api.PlayerActivatedEvent
import com.spruhs.kick_app.group.api.PlayerDeactivatedEvent
import com.spruhs.kick_app.group.api.PlayerDowngradedEvent
import com.spruhs.kick_app.group.api.PlayerEnteredGroupEvent
import com.spruhs.kick_app.group.api.PlayerLeavedEvent
import com.spruhs.kick_app.group.api.PlayerPromotedEvent
import com.spruhs.kick_app.group.api.PlayerRemovedEvent
import com.spruhs.kick_app.view.core.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component


@Component("ViewGroupListener")
class GroupListener(
    private val applicationScope: CoroutineScope,
    private val userService: UserService,
) {

    private val log = getLogger(this::class.java)

    @EventListener(
        GroupNameChangedEvent::class,
        GroupCreatedEvent::class,
        PlayerEnteredGroupEvent::class,
        PlayerLeavedEvent::class,
        PlayerActivatedEvent::class,
        PlayerDeactivatedEvent::class,
        PlayerRemovedEvent::class,
        PlayerPromotedEvent::class,
        PlayerDowngradedEvent::class
    )
    fun onEvent(event: BaseEvent) {
        log.info("User scope received: $event")
        applicationScope.launch {
            userService.whenEvent(event)
        }
    }
}