package com.spruhs.kick_app.match.core.adapter.primary

import com.spruhs.kick_app.common.configs.EventExecutionStrategy
import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchNumberChangedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.PlayerOverviewUpdatedEvent
import com.spruhs.kick_app.match.core.application.MatchCommandPort
import com.spruhs.kick_app.match.core.application.MatchOverviewCommandPort
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component("MatchMatchListener")
class MatchListener(
    private val eventExecutionStrategy: EventExecutionStrategy,
    private val matchCommandPort: MatchCommandPort,
    private val matchOverviewCommandPort: MatchOverviewCommandPort,
) {
    @EventListener(MatchNumberChangedEvent::class)
    fun onMatchEvent(event: BaseEvent) {
        eventExecutionStrategy.execute {
            matchCommandPort.onEvent(event)
        }
    }

    @EventListener(MatchCanceledEvent::class,
        MatchResultEnteredEvent::class)
    fun onMatchOverviewEvent(event: BaseEvent) {
        eventExecutionStrategy.execute {
            matchOverviewCommandPort.onEvent(event)
        }
    }
}
