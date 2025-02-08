package com.spruhs.kick_app.match.api

import com.spruhs.kick_app.common.DomainEvent
import java.time.LocalDateTime

data class MatchCreatedEvent(
    val groupId: String,
    val matchId: String,
    val start: LocalDateTime
) : DomainEvent {
    override fun eventVersion(): Int = 1
}