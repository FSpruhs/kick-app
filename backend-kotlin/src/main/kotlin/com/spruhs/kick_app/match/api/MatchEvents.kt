package com.spruhs.kick_app.match.api

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.DomainEvent
import com.spruhs.kick_app.common.GroupId
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

data class MatchPlannedEvent(
    override val aggregateId: String,
    val groupId: GroupId,
    val start: LocalDateTime,
    val playground: String,
    val maxPlayer: Int,
    val minPlayer: Int
) : BaseEvent(aggregateId)

data class PlayerAddedToCadreEvent(
    override val aggregateId: String,
) : BaseEvent(aggregateId)

data class PlayerDeregisteredEvent(
    override val aggregateId: String,
) : BaseEvent(aggregateId)

data class PlayerPlacedOnSubstituteBenchEvent(
    override val aggregateId: String,
) : BaseEvent(aggregateId)

data class MatchCanceledEvent(
    override val aggregateId: String,
) : BaseEvent(aggregateId)

data class PlaygroundChangedEvent(
    override val aggregateId: String,
) : BaseEvent(aggregateId)

data class MatchResultEnteredEvent(
    override val aggregateId: String,
) : BaseEvent(aggregateId)

data class MatchStartedEvent(
    override val aggregateId: String,
) : BaseEvent(aggregateId)