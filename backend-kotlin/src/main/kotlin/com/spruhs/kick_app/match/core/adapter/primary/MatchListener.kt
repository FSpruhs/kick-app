package com.spruhs.kick_app.match.core.adapter.primary

import com.spruhs.kick_app.common.configs.EventExecutionStrategy
import com.spruhs.kick_app.match.api.MatchNumberChangedEvent
import com.spruhs.kick_app.match.core.application.MatchCommandPort
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component("MatchMatchListener")
class MatchListener(
    private val eventExecutionStrategy: EventExecutionStrategy,
    private val matchCommandPort: MatchCommandPort,
) {
    @EventListener(MatchNumberChangedEvent::class)
    fun onEvent(event: MatchNumberChangedEvent) {
        eventExecutionStrategy.execute {
            matchCommandPort.changeMatchNumber(event)
        }
    }
}
