package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.user.core.application.MessageParams
import com.spruhs.kick_app.user.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.domain.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component("UserMatchListener")
class MatchListener(
    private val messageUseCases: MessageUseCases,
    private val applicationScope: CoroutineScope
) {

    @EventListener(MatchPlannedEvent::class)
    fun onEvent(event: MatchPlannedEvent) {
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
        applicationScope.launch {
            messageUseCases.send(
                messageType = MessageType.PLAYER_ADDED_TO_CADRE,
                params = event.toMessageParams(),
            )
        }
    }

    @EventListener(PlayerPlacedOnWaitingBenchEvent::class)
    fun onEvent(event: PlayerPlacedOnWaitingBenchEvent) {
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