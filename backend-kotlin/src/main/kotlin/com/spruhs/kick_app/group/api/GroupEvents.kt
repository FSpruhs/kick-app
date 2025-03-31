package com.spruhs.kick_app.group.api

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.DomainEvent

data class UserInvitedToGroupEvent(
    val inviteeId: String,
    val groupName: String,
    val groupId: String,
) : DomainEvent {
    override fun eventVersion(): Int = 1
}

data class UserLeavedGroupEvent(
    val userId: String,
    val groupId: String,
    val groupName: String,
) : DomainEvent {
    override fun eventVersion(): Int = 1
}

data class UserRemovedFromGroupEvent(
    val userId: String,
    val groupId: String,
    val groupName: String,
) : DomainEvent {
    override fun eventVersion(): Int = 1
}

data class UserEnteredGroupEvent(
    val userId: String,
    val groupId: String,
) : DomainEvent {
    override fun eventVersion(): Int = 1
}

data class PlayerStatusUpdated(
    val userId: String,
    val groupId: String,
    val groupName: String,
    val newStatus: String,

    ) : DomainEvent {
    override fun eventVersion(): Int = 1
}

data class GroupCreatedEvent(
    override val aggregateId: String,
    val name: String,
): BaseEvent(aggregateId)

data class GroupNameChangedEvent(
    override val aggregateId: String,
    val name: String,
): BaseEvent(aggregateId)

data class PlayerInvitedEvent(
    override val aggregateId: String,
    val userId: String
): BaseEvent(aggregateId)

data class PlayerEnteredGroupEvent(
    override val aggregateId: String,
    val userId: String
): BaseEvent(aggregateId)

data class PlayerRejectedGroupEvent(
    override val aggregateId: String,
    val userId: String
): BaseEvent(aggregateId)

data class PlayerPromotedEvent(
    override val aggregateId: String,
    val userId: String
): BaseEvent(aggregateId)

data class PlayerDowngradedEvent(
    override val aggregateId: String,
    val userId: String
): BaseEvent(aggregateId)

data class PlayerActivatedEvent(
    override val aggregateId: String,
    val userId: String
): BaseEvent(aggregateId)

data class PlayerDeactivatedEvent(
    override val aggregateId: String,
    val userId: String
): BaseEvent(aggregateId)

data class PlayerRemovedEvent(
    override val aggregateId: String,
    val userId: String
): BaseEvent(aggregateId)

data class PlayerLeavedEvent(
    override val aggregateId: String,
    val userId: String
): BaseEvent(aggregateId)