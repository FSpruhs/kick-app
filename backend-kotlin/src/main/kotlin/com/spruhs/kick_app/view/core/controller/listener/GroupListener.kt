package com.spruhs.kick_app.view.core.controller.listener

import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.configs.EventExecutionStrategy
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
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component


@Component("ViewGroupListener")
class GroupListener(
    private val eventExecutionStrategy: EventExecutionStrategy,
    private val userService: UserService,
    private val groupService: GroupService,
    private val statisticService: StatisticService
) {

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
        eventExecutionStrategy.execute {
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
        eventExecutionStrategy.execute {
            userService.whenEvent(event)
        }
    }

    @EventListener(
        GroupCreatedEvent::class,
        PlayerEnteredGroupEvent::class
    )
    fun onStatisticRelevantEvent(event: BaseEvent) {
        eventExecutionStrategy.execute {
            statisticService.whenEvent(event)
        }
    }
}