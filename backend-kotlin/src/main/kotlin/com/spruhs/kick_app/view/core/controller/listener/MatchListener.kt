package com.spruhs.kick_app.view.core.controller.listener

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.view.core.service.MatchService
import com.spruhs.kick_app.view.core.service.StatisticService
import com.spruhs.kick_app.view.core.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component("ViewMatchListener")
class MatchListener(
    private val applicationScope: CoroutineScope,
    private val userService: UserService,
    private val matchService: MatchService,
    private val statisticService: StatisticService
) {
    @EventListener(MatchResultEnteredEvent::class)
    fun onStatisticRelevantEvent(event: MatchResultEnteredEvent) {
        applicationScope.launch {
            statisticService.whenEvent(event)
        }
    }

    @EventListener(MatchResultEnteredEvent::class)
    fun onUserRelevantEvent(event: BaseEvent) {
        applicationScope.launch {
            userService.whenEvent(event)
        }
    }

    @EventListener(
        MatchResultEnteredEvent::class,
        MatchCanceledEvent::class,
        PlayerAddedToCadreEvent::class,
        MatchPlannedEvent::class,
        PlayerPlacedOnWaitingBenchEvent::class,
        PlaygroundChangedEvent::class,
        PlayerDeregisteredEvent::class,
    )
    fun onMatchRelevantEvent(event: BaseEvent) {
        applicationScope.launch {
            matchService.whenEvent(event)
        }
    }
}