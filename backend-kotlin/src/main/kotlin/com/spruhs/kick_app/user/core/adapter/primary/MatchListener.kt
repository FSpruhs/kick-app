package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.user.core.application.MessageParams
import com.spruhs.kick_app.user.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.domain.MessageType
import com.spruhs.kick_app.user.core.domain.UserProjectionPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class MatchListener(
    private val messageUseCases: MessageUseCases,
    private val userProjectionPort: UserProjectionPort,
    private val applicationScope: CoroutineScope
) {

    private val log = getLogger(this::class.java)

    @EventListener(MatchResultEnteredEvent::class)
    fun onEvent(event: MatchResultEnteredEvent) {
        log.info("MatchResultEnteredEvent received: $event")
        applicationScope.launch {
            userProjectionPort.whenEvent(event)
        }
    }

    @EventListener(MatchPlannedEvent::class)
    fun onEvent(event: MatchPlannedEvent) {
        log.info("MatchPlannedEvent received: $event")
        applicationScope.launch {
            messageUseCases.sendAllActiveUsersInGroupMessage(
                messageType = MessageType.MATCH_CREATED,
                params = event.toMessageParams(),
                groupId = event.groupId
            )
        }
    }

    @EventListener(MatchCanceledEvent::class)
    fun onEvent(event: MatchCanceledEvent) {
        log.info("MatchCanceledEvent received: $event")
        applicationScope.launch {
            messageUseCases.sendAllActiveUsersInGroupMessage(
                messageType = MessageType.MATCH_CANCELED,
                params = event.toMessageParams(),
                groupId = event.groupId
            )
        }
    }

    @EventListener(PlaygroundChangedEvent::class)
    fun onEvent(event: PlaygroundChangedEvent) {
        log.info("PlaygroundChangedEvent received: $event")
        applicationScope.launch {
            messageUseCases.sendAllActiveUsersInGroupMessage(
                messageType = MessageType.PLAYGROUND_CHANGED,
                params = event.toMessageParams(),
                groupId = event.groupId
            )
        }
    }

    @EventListener(PlayerAddedToCadreEvent::class)
    fun onEvent(event: PlayerAddedToCadreEvent) {
        log.info("PlayerAddedToCadreEvent received: $event")
        applicationScope.launch {
            messageUseCases.send(
                messageType = MessageType.PLAYER_ADDED_TO_CADRE,
                params = event.toMessageParams(),
            )
        }
    }

    @EventListener(PlayerPlacedOnWaitingBenchEvent::class)
    fun onEvent(event: PlayerPlacedOnWaitingBenchEvent) {
        log.info("PlayerPlacedOnWaitingBenchEvent received: $event")
        applicationScope.launch {
            messageUseCases.send(
                messageType = MessageType.PLAYER_PLACED_ON_WAITING_BENCH,
                params = event.toMessageParams(),
            )
        }
    }
}

private fun PlaygroundChangedEvent.toMessageParams() = MessageParams(
    matchId = MatchId(this.aggregateId),
    groupId = this.groupId,
    playground = this.newPlayground
)

private fun MatchCanceledEvent.toMessageParams() = MessageParams(
    matchId = MatchId(this.aggregateId),
    groupId = this.groupId
)

private fun PlayerPlacedOnWaitingBenchEvent.toMessageParams() = MessageParams(
    matchId = MatchId(this.aggregateId),
    userId = this.userId
)

private fun PlayerAddedToCadreEvent.toMessageParams() = MessageParams(
    matchId = MatchId(this.aggregateId),
    userId = this.userId
)

private fun MatchPlannedEvent.toMessageParams() = MessageParams(
    matchId = MatchId(this.aggregateId),
    start = this.start,
    groupId = this.groupId
)