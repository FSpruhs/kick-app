package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.user.core.application.MessageParams
import com.spruhs.kick_app.user.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.domain.MessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class MatchListener(
    private val messageUseCases: MessageUseCases,
    private val applicationScope: CoroutineScope
) {

    private val log = getLogger(this::class.java)

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
}

private fun MatchPlannedEvent.toMessageParams() = MessageParams(
    matchId = this.aggregateId,
    start = this.start,
    groupId = this.groupId.value
)