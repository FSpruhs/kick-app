package com.spruhs.kick_app.view.core.controller.listener

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.view.core.service.MatchService
import com.spruhs.kick_app.view.core.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component("ViewMatchListener")
class MatchListener(
    private val applicationScope: CoroutineScope,
    private val userService: UserService,
    private val matchService: MatchService
) {

    private val log = getLogger(this::class.java)

    @EventListener(MatchResultEnteredEvent::class)
    fun onEvent(event: MatchResultEnteredEvent) {
        log.info("MatchResultEnteredEvent received: $event")
        applicationScope.launch {
            userService.whenEvent(event)
        }
        applicationScope.launch {
            matchService.whenEvent(event)
        }
    }

    @EventListener(
        MatchCanceledEvent::class,
        PlayerAddedToCadreEvent::class,
        MatchPlannedEvent::class,
        PlayerPlacedOnWaitingBenchEvent::class,
        PlaygroundChangedEvent::class,
        PlayerDeregisteredEvent::class,
    )
    fun onEvent(event: BaseEvent) {
        log.info("Match scope received: $event")
        applicationScope.launch {
            matchService.whenEvent(event)
        }
    }
}