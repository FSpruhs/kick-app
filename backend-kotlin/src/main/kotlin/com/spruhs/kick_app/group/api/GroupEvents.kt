package com.spruhs.kick_app.group.api

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.group.core.domain.GroupAggregate
import org.springframework.stereotype.Component

data class GroupCreatedEvent(
    override val aggregateId: String,
    val userId: UserId,
    val name: String,
): BaseEvent(aggregateId)

data class GroupNameChangedEvent(
    override val aggregateId: String,
    val name: String,
): BaseEvent(aggregateId)

data class PlayerInvitedEvent(
    override val aggregateId: String,
    val userId: UserId,
    val name: String,
): BaseEvent(aggregateId)

data class PlayerEnteredGroupEvent(
    override val aggregateId: String,
    val userId: UserId,
    val name: String
): BaseEvent(aggregateId)

data class PlayerRejectedGroupEvent(
    override val aggregateId: String,
    val userId: UserId
): BaseEvent(aggregateId)

data class PlayerPromotedEvent(
    override val aggregateId: String,
    val userId: UserId
): BaseEvent(aggregateId)

data class PlayerDowngradedEvent(
    override val aggregateId: String,
    val userId: UserId
): BaseEvent(aggregateId)

data class PlayerActivatedEvent(
    override val aggregateId: String,
    val userId: UserId
): BaseEvent(aggregateId)

data class PlayerDeactivatedEvent(
    override val aggregateId: String,
    val userId: UserId
): BaseEvent(aggregateId)

data class PlayerRemovedEvent(
    override val aggregateId: String,
    val userId: UserId,
    val name: String
): BaseEvent(aggregateId)

data class PlayerLeavedEvent(
    override val aggregateId: String,
    val userId: UserId
): BaseEvent(aggregateId)

enum class GroupEvents {
    GROUP_CREATED_V1,
    GROUP_NAME_CHANGED_V1,
    PLAYER_INVITED_V1,
    PLAYER_ENTERED_GROUP_V1,
    PLAYER_REJECTED_GROUP_V1,
    PLAYER_PROMOTED_V1,
    PLAYER_DOWNGRADED_V1,
    PLAYER_ACTIVATED_V1,
    PLAYER_DEACTIVATED_V1,
    PLAYER_REMOVED_V1,
    PLAYER_LEAVED_V1
}

@Component
class GroupEventSerializer : Serializer {
    override fun serialize(event: Any, aggregate: AggregateRoot): Event {
        val data = EventSourcingUtils.writeValueAsBytes(event)

        return when (event) {
            is GroupCreatedEvent -> Event(
                aggregate,
                GroupEvents.GROUP_CREATED_V1.name,
                data,
                event.metadata
            )

            is GroupNameChangedEvent -> Event(
                aggregate,
                GroupEvents.GROUP_NAME_CHANGED_V1.name,
                data,
                event.metadata
            )

            is PlayerInvitedEvent -> Event(
                aggregate,
                GroupEvents.PLAYER_INVITED_V1.name,
                data,
                event.metadata
            )

            is PlayerEnteredGroupEvent -> Event(
                aggregate,
                GroupEvents.PLAYER_ENTERED_GROUP_V1.name,
                data,
                event.metadata
            )

            is PlayerRejectedGroupEvent -> Event(
                aggregate,
                GroupEvents.PLAYER_REJECTED_GROUP_V1.name,
                data,
                event.metadata
            )

            is PlayerPromotedEvent -> Event(
                aggregate,
                GroupEvents.PLAYER_PROMOTED_V1.name,
                data,
                event.metadata
            )

            is PlayerDowngradedEvent -> Event(
                aggregate,
                GroupEvents.PLAYER_DOWNGRADED_V1.name,
                data,
                event.metadata
            )

            is PlayerActivatedEvent -> Event(
                aggregate,
                GroupEvents.PLAYER_ACTIVATED_V1.name,
                data,
                event.metadata
            )

            is PlayerDeactivatedEvent -> Event(
                aggregate,
                GroupEvents.PLAYER_DEACTIVATED_V1.name,
                data,
                event.metadata
            )

            is PlayerRemovedEvent -> Event(
                aggregate,
                GroupEvents.PLAYER_REMOVED_V1.name,
                data,
                event.metadata
            )

            is PlayerLeavedEvent -> Event(
                aggregate,
                GroupEvents.PLAYER_LEAVED_V1.name,
                data,
                event.metadata
            )

            else -> throw UnknownEventTypeException(event)
        }
    }

    override fun deserialize(event: Event): Any {
        return when (event.type) {
            GroupEvents.GROUP_CREATED_V1.name -> EventSourcingUtils.readValue(
                event.data, GroupCreatedEvent::class.java
            )

            GroupEvents.GROUP_NAME_CHANGED_V1.name -> EventSourcingUtils.readValue(
                event.data, GroupNameChangedEvent::class.java
            )

            GroupEvents.PLAYER_INVITED_V1.name -> EventSourcingUtils.readValue(
                event.data, PlayerInvitedEvent::class.java
            )

            GroupEvents.PLAYER_ENTERED_GROUP_V1.name -> EventSourcingUtils.readValue(
                event.data, PlayerEnteredGroupEvent::class.java
            )

            GroupEvents.PLAYER_REJECTED_GROUP_V1.name -> EventSourcingUtils.readValue(
                event.data, PlayerRejectedGroupEvent::class.java
            )

            GroupEvents.PLAYER_PROMOTED_V1.name -> EventSourcingUtils.readValue(
                event.data, PlayerPromotedEvent::class.java
            )

            GroupEvents.PLAYER_DOWNGRADED_V1.name -> EventSourcingUtils.readValue(
                event.data, PlayerDowngradedEvent::class.java
            )

            GroupEvents.PLAYER_ACTIVATED_V1.name -> EventSourcingUtils.readValue(
                event.data, PlayerActivatedEvent::class.java
            )

            GroupEvents.PLAYER_DEACTIVATED_V1.name -> EventSourcingUtils.readValue(
                event.data, PlayerDeactivatedEvent::class.java
            )

            GroupEvents.PLAYER_REMOVED_V1.name -> EventSourcingUtils.readValue(
                event.data, PlayerRemovedEvent::class.java
            )

            GroupEvents.PLAYER_LEAVED_V1.name -> EventSourcingUtils.readValue(
                event.data, PlayerLeavedEvent::class.java
            )

            else -> throw UnknownEventTypeException(event)
        }
    }

    override fun aggregateTypeName(): String {
        return GroupAggregate::class.simpleName ?: "GroupAggregate"
    }
}