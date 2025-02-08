package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.match.api.MatchCreatedEvent
import com.spruhs.kick_app.user.core.application.MessageParams
import com.spruhs.kick_app.user.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.domain.MessageType
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class MatchListener(
    private val messageUseCases: MessageUseCases
) {

    private val log = getLogger(this::class.java)

    @EventListener(MatchCreatedEvent::class)
    fun onEvent(event: MatchCreatedEvent) {
        log.info("MatchCreatedEvent received: $event")
        messageUseCases.sendAllActiveUsersInGroupMessage(
            messageType = MessageType.MATCH_CREATED,
            params = event.toMessageParams(),
            groupId = GroupId(event.groupId)
        )
    }
}

private fun MatchCreatedEvent.toMessageParams() = MessageParams(
    matchId = this.matchId,
    start = this.start,
    groupId = this.groupId
)