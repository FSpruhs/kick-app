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
import com.spruhs.kick_app.view.core.service.GroupService
import com.spruhs.kick_app.view.core.service.StatisticService
import com.spruhs.kick_app.view.core.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component


@Component("ViewGroupListener")
class GroupListener(
    private val applicationScope: CoroutineScope,
    private val userService: UserService,
    private val groupService: GroupService,
    private val statisticService: StatisticService
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
    fun onGroupRelevantEvent(event: BaseEvent) {
        log.info("ViewGroupListener received: $event")
        applicationScope.launch {
            groupService.whenEvent(event)
        }
    }

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
    fun onUserRelevantEvent(event: BaseEvent) {
        log.info("User relevant event received: $event")
        applicationScope.launch {
            userService.whenEvent(event)
        }
    }

    @EventListener(
        GroupCreatedEvent::class,
        PlayerEnteredGroupEvent::class
    )
    fun onStatisticRelevantEvent(event: BaseEvent) {
        log.info("Group scope received: $event")
        applicationScope.launch {
            statisticService.whenEvent(event)
        }
    }
}