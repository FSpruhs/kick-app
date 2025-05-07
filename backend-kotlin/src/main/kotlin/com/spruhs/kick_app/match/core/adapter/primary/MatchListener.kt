package com.spruhs.kick_app.match.core.adapter.primary

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.match.core.domain.MatchProjectionPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component("MatchMatchListener")
class MatchListener(
    private val matchProjectionPort: MatchProjectionPort,
    private val applicationScope: CoroutineScope
) {
    private val log = getLogger(this::class.java)

    @EventListener(
        MatchCanceledEvent::class,
        MatchResultEnteredEvent::class,
        PlayerAddedToCadreEvent::class,
        MatchPlannedEvent::class,
        PlayerPlacedOnWaitingBenchEvent::class,
        PlaygroundChangedEvent::class,
        PlayerDeregisteredEvent::class,
        )
    fun onEvent(event: BaseEvent) {
        log.info("Match scoe received: $event")
        applicationScope.launch {
            matchProjectionPort.whenEvent(event)
        }
    }
}