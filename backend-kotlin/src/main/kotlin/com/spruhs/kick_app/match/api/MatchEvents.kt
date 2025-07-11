package com.spruhs.kick_app.match.api

import com.spruhs.kick_app.common.es.AggregateRoot
import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.es.Event
import com.spruhs.kick_app.common.es.EventSourcingUtils
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.es.Serializer
import com.spruhs.kick_app.common.es.UnknownEventTypeException
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.match.core.domain.MatchAggregate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

data class MatchPlannedEvent(
    override val aggregateId: String,
    val groupId: GroupId,
    val start: LocalDateTime,
    val playground: String? = null,
    val maxPlayer: Int,
    val minPlayer: Int
) : BaseEvent(aggregateId)

data class PlayerAddedToCadreEvent(
    override val aggregateId: String,
    val userId: UserId,
    val status: String,
) : BaseEvent(aggregateId)

data class PlayerDeregisteredEvent(
    override val aggregateId: String,
    val userId: UserId,
    val status: String,
) : BaseEvent(aggregateId)

data class PlayerPlacedOnWaitingBenchEvent(
    override val aggregateId: String,
    val userId: UserId,
    val status: String,
) : BaseEvent(aggregateId)

data class MatchCanceledEvent(
    override val aggregateId: String,
    val groupId: GroupId,
) : BaseEvent(aggregateId)

data class PlaygroundChangedEvent(
    override val aggregateId: String,
    val newPlayground: String,
    val groupId: GroupId
) : BaseEvent(aggregateId)

data class MatchResultEnteredEvent(
    override val aggregateId: String,
    val groupId: GroupId,
    val players: List<ParticipatingPlayer>,
    val start: LocalDateTime,
) : BaseEvent(aggregateId)

enum class MatchEvents {
    MATCH_PLANNED_V1,
    PLAYER_ADDED_TO_CADRE_V1,
    PLAYER_DEREGISTERED_V1,
    PLAYER_PLACED_ON_WAITING_BENCH_V1,
    MATCH_CANCELED_V1,
    PLAYGROUND_CHANGED_V1,
    MATCH_RESULT_ENTERED_V1,
}

@Component
class MatchEventSerializer : Serializer {
    override fun serialize(event: Any, aggregate: AggregateRoot): Event {
        val data = EventSourcingUtils.writeValueAsBytes(event)

        return when (event) {
            is MatchPlannedEvent -> Event(
                aggregate,
                MatchEvents.MATCH_PLANNED_V1.name,
                data,
                event.metadata
            )
            is PlayerAddedToCadreEvent -> Event(
                aggregate,
                MatchEvents.PLAYER_ADDED_TO_CADRE_V1.name,
                data,
                event.metadata
            )
            is PlayerDeregisteredEvent -> Event(
                aggregate,
                MatchEvents.PLAYER_DEREGISTERED_V1.name,
                data,
                event.metadata
            )
            is PlayerPlacedOnWaitingBenchEvent -> Event(
                aggregate,
                MatchEvents.PLAYER_PLACED_ON_WAITING_BENCH_V1.name,
                data,
                event.metadata
            )
            is MatchCanceledEvent -> Event(
                aggregate,
                MatchEvents.MATCH_CANCELED_V1.name,
                data,
                event.metadata
            )
            is PlaygroundChangedEvent -> Event(
                aggregate,
                MatchEvents.PLAYGROUND_CHANGED_V1.name,
                data,
                event.metadata
            )
            is MatchResultEnteredEvent -> Event(
                aggregate,
                MatchEvents.MATCH_RESULT_ENTERED_V1.name,
                data,
                event.metadata
            )

            else -> throw UnknownEventTypeException(event)
        }
    }

    override fun deserialize(event: Event): BaseEvent {
        return when (event.type) {
            MatchEvents.MATCH_PLANNED_V1.name -> EventSourcingUtils.readValue(
                event.data, MatchPlannedEvent::class.java
            )
            MatchEvents.PLAYER_ADDED_TO_CADRE_V1.name -> EventSourcingUtils.readValue(
                event.data, PlayerAddedToCadreEvent::class.java
            )
            MatchEvents.PLAYER_DEREGISTERED_V1.name -> EventSourcingUtils.readValue(
                event.data, PlayerDeregisteredEvent::class.java
            )
            MatchEvents.PLAYER_PLACED_ON_WAITING_BENCH_V1.name -> EventSourcingUtils.readValue(
                event.data, PlayerPlacedOnWaitingBenchEvent::class.java
            )
            MatchEvents.MATCH_CANCELED_V1.name -> EventSourcingUtils.readValue(
                event.data, MatchCanceledEvent::class.java
            )
            MatchEvents.PLAYGROUND_CHANGED_V1.name -> EventSourcingUtils.readValue(
                event.data, PlaygroundChangedEvent::class.java
            )
            MatchEvents.MATCH_RESULT_ENTERED_V1.name -> EventSourcingUtils.readValue(
                event.data, MatchResultEnteredEvent::class.java
            )

            else -> throw UnknownEventTypeException(event)
        }
    }

    override fun aggregateTypeName(): String {
        return MatchAggregate::class.simpleName ?: "MatchAggregate"
    }
}

enum class PlayerResult {
    WIN, LOSS, DRAW
}

enum class MatchTeam {
    A, B
}
data class ParticipatingPlayer(
    val userId: UserId,
    val playerResult: PlayerResult,
    val team: MatchTeam
)