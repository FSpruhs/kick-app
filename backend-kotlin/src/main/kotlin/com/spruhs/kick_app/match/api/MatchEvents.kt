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

data class MatchCancelledEvent(
    val matchId: String
) : DomainEvent {
    override fun eventVersion(): Int = 1
}

data class ResultAddedEvent(
    val matchId: String,
    val result: String,
    val teamA: List<String>,
    val teamB: List<String>
) : DomainEvent {
    override fun eventVersion(): Int = 1
}