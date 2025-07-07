package com.spruhs.kick_app.view.core.controller.listener

import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.configs.EventExecutionStrategy
import com.spruhs.kick_app.user.api.UserCreatedEvent
import com.spruhs.kick_app.user.api.UserImageUpdatedEvent
import com.spruhs.kick_app.user.api.UserNickNameChangedEvent
import com.spruhs.kick_app.view.core.service.GroupService
import com.spruhs.kick_app.view.core.service.UserService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component("ViewUserListener")
class UserListener(
    private val eventExecutionStrategy: EventExecutionStrategy,
    private val userService: UserService,
    private val groupService: GroupService,
) {

    @EventListener(
        UserCreatedEvent::class,
        UserNickNameChangedEvent::class,
        UserImageUpdatedEvent::class,
    )
    fun onUserRelevantEvent(event: BaseEvent) {
        eventExecutionStrategy.execute {
            userService.whenEvent(event)
        }
    }

    @EventListener(
        UserNickNameChangedEvent::class,
        UserImageUpdatedEvent::class,
    )
    fun onUerRelevantEvent(event: BaseEvent) {
        eventExecutionStrategy.execute {
            groupService.whenEvent(event)
        }
    }
}