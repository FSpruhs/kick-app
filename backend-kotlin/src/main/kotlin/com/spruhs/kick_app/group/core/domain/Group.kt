package com.spruhs.kick_app.group.core.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.spruhs.kick_app.common.es.AggregateRoot
import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.es.UnknownEventTypeException
import com.spruhs.kick_app.common.exceptions.PlayerNotFoundException
import com.spruhs.kick_app.common.exceptions.UserNotAuthorizedException
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.group.api.GroupCreatedEvent
import com.spruhs.kick_app.group.api.GroupNameChangedEvent
import com.spruhs.kick_app.group.api.PlayerActivatedEvent
import com.spruhs.kick_app.group.api.PlayerDeactivatedEvent
import com.spruhs.kick_app.group.api.PlayerDowngradedEvent
import com.spruhs.kick_app.group.api.PlayerEnteredGroupEvent
import com.spruhs.kick_app.group.api.PlayerInvitedEvent
import com.spruhs.kick_app.group.api.PlayerLeavedEvent
import com.spruhs.kick_app.group.api.PlayerPromotedEvent
import com.spruhs.kick_app.group.api.PlayerRejectedGroupEvent
import com.spruhs.kick_app.group.api.PlayerRemovedEvent

class GroupAggregate(
    override val aggregateId: String,
) : AggregateRoot(aggregateId, TYPE) {
    var name: Name = Name("Default")
    var players: MutableList<Player> = mutableListOf()
    var invitedUsers: MutableSet<UserId> = mutableSetOf()

    override fun whenEvent(event: BaseEvent) {
        when (event) {
            is GroupCreatedEvent -> handleGroupCreatedEvent(event)
            is GroupNameChangedEvent -> handleGroupNameChangedEvent(event)
            is PlayerInvitedEvent -> handlePlayerInvitedEvent(event)
            is PlayerEnteredGroupEvent -> handlePlayerEnteredGroupEvent(event)
            is PlayerRejectedGroupEvent -> handlePlayerRejectedGroupEvent(event)
            is PlayerPromotedEvent -> handlePlayerRoleEvent(event.userId, PlayerRole.COACH)
            is PlayerDowngradedEvent -> handlePlayerRoleEvent(event.userId, PlayerRole.PLAYER)
            is PlayerActivatedEvent -> handlePlayerStatusEvent(event.userId, Active())
            is PlayerDeactivatedEvent -> handlePlayerStatusEvent(event.userId, Inactive())
            is PlayerRemovedEvent -> handlePlayerStatusEvent(event.userId, Removed())
            is PlayerLeavedEvent -> handlePlayerStatusEvent(event.userId, Leaved())

            else -> throw UnknownEventTypeException(event)
        }
    }

    private fun handleGroupCreatedEvent(event: GroupCreatedEvent) {
        name = Name(event.name)
        players.add(Player(event.userId, Active(), PlayerRole.COACH))
    }

    private fun handleGroupNameChangedEvent(event: GroupNameChangedEvent) {
        name = Name(event.name)
    }

    private fun handlePlayerInvitedEvent(event: PlayerInvitedEvent) {
        invitedUsers.add(event.userId)
    }

    private fun handlePlayerEnteredGroupEvent(event: PlayerEnteredGroupEvent) {
        players.add(Player(event.userId, Active(), event.userRole))
        invitedUsers -= event.userId
    }

    private fun handlePlayerRejectedGroupEvent(event: PlayerRejectedGroupEvent) {
        invitedUsers -= event.userId
    }

    private fun handlePlayerRoleEvent(
        userId: UserId,
        playerRole: PlayerRole,
    ) {
        players.find { it.id == userId }?.let {
            players.remove(it)
            players.add(it.copy(role = playerRole))
        }
    }

    private fun handlePlayerStatusEvent(
        userId: UserId,
        status: PlayerStatus,
    ) {
        players.find { it.id == userId }?.let {
            players.remove(it)
            players.add(it.copy(status = status))
        }
    }

    fun createGroup(
        userId: UserId,
        name: Name,
    ) {
        apply(
            GroupCreatedEvent(
                aggregateId = aggregateId,
                userId = userId,
                name = name.value,
                userStatus = PlayerStatusType.ACTIVE,
                userRole = PlayerRole.COACH,
            ),
        )
    }

    fun changeGroupName(
        userId: UserId,
        newName: Name,
    ) {
        require(this.players.find { it.id == userId }?.role == PlayerRole.COACH) {
            throw UserNotAuthorizedException(userId)
        }

        apply(GroupNameChangedEvent(aggregateId, newName.value))
    }

    fun inviteUser(
        inviterId: UserId,
        inviteeId: UserId,
    ) {
        this.players.find { it.id == inviterId && it.status.type() == PlayerStatusType.ACTIVE }
            ?: throw UserNotAuthorizedException(inviterId)

        if (inviteeId in this.players.map { it.id }) {
            throw PlayerAlreadyInGroupException(inviteeId)
        }

        apply(PlayerInvitedEvent(aggregateId, inviteeId, name.value))
    }

    fun inviteUserResponse(
        userId: UserId,
        response: Boolean,
    ) {
        if (userId !in this.invitedUsers) {
            throw PlayerNotInvitedInGroupException(userId)
        }

        if (response) {
            apply(
                PlayerEnteredGroupEvent(
                    aggregateId = aggregateId,
                    userId = userId,
                    groupName = name.value,
                    userStatus = PlayerStatusType.ACTIVE,
                    userRole = PlayerRole.PLAYER,
                ),
            )
        } else {
            apply(PlayerRejectedGroupEvent(aggregateId, userId))
        }
    }

    fun updatePlayerRole(
        userId: UserId,
        updatingUserId: UserId,
        newRole: PlayerRole,
    ) {
        players.find { it.id == updatingUserId && it.role == PlayerRole.COACH }
            ?: throw UserNotAuthorizedException(updatingUserId)

        val player = players.find { it.id == userId } ?: throw PlayerNotFoundException(userId)

        if (newRole == PlayerRole.COACH && player.role != PlayerRole.COACH) {
            apply(PlayerPromotedEvent(aggregateId, userId))
        } else if (newRole == PlayerRole.PLAYER && player.role != PlayerRole.PLAYER) {
            apply(PlayerDowngradedEvent(aggregateId, userId))
        }
    }

    fun updatePlayerStatus(
        userId: UserId,
        updatingUserId: UserId,
        newStatus: PlayerStatusType,
    ) {
        val player = players.find { it.id == userId } ?: throw PlayerNotFoundException(userId)
        val requestingPlayer =
            if (userId == updatingUserId) {
                player
            } else {
                players.find { it.id == updatingUserId }
                    ?: throw PlayerNotFoundException(updatingUserId)
            }

        val updatedStatus =
            when (newStatus) {
                PlayerStatusType.ACTIVE -> player.status.activate(player, requestingPlayer)
                PlayerStatusType.INACTIVE -> player.status.inactivate(player, requestingPlayer)
                PlayerStatusType.LEAVED -> player.status.leave(player, requestingPlayer)
                PlayerStatusType.REMOVED -> player.status.remove(player, requestingPlayer)
            }

        when (updatedStatus) {
            is Active -> apply(PlayerActivatedEvent(aggregateId, userId))
            is Inactive -> apply(PlayerDeactivatedEvent(aggregateId, userId))
            is Leaved -> apply(PlayerLeavedEvent(aggregateId, userId))
            is Removed -> apply(PlayerRemovedEvent(aggregateId, userId, name.value))
        }
    }

    companion object {
        const val TYPE = "Group"
    }
}

data class Player(
    val id: UserId,
    val status: PlayerStatus,
    val role: PlayerRole,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = Active::class, name = "ACTIVE"),
    JsonSubTypes.Type(value = Inactive::class, name = "INACTIVE"),
    JsonSubTypes.Type(value = Leaved::class, name = "LEAVED"),
    JsonSubTypes.Type(value = Removed::class, name = "REMOVED"),
)
interface PlayerStatus {
    fun activate(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus

    fun inactivate(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus

    fun leave(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus

    fun remove(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus

    fun type(): PlayerStatusType
}

class Active : PlayerStatus {
    override fun activate(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus = this

    override fun inactivate(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus = Inactive()

    override fun leave(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus {
        require(player == requestingPlayer)
        return Leaved()
    }

    override fun remove(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus {
        require(player != requestingPlayer)
        require(requestingPlayer.role == PlayerRole.COACH)
        return Removed()
    }

    override fun type(): PlayerStatusType = PlayerStatusType.ACTIVE
}

class Inactive : PlayerStatus {
    override fun activate(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus = Active()

    override fun inactivate(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus = this

    override fun leave(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus {
        require(player == requestingPlayer)
        return Leaved()
    }

    override fun remove(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus {
        require(player != requestingPlayer)
        require(requestingPlayer.role == PlayerRole.COACH)
        return Removed()
    }

    override fun type(): PlayerStatusType = PlayerStatusType.INACTIVE
}

class Leaved : PlayerStatus {
    override fun activate(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus {
        require(player == requestingPlayer)
        return Active()
    }

    override fun inactivate(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus = this

    override fun leave(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus = this

    override fun remove(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus {
        require(player != requestingPlayer)
        require(requestingPlayer.role == PlayerRole.COACH)
        return Removed()
    }

    override fun type(): PlayerStatusType = PlayerStatusType.LEAVED
}

class Removed : PlayerStatus {
    override fun activate(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus {
        require(player != requestingPlayer)
        require(requestingPlayer.role == PlayerRole.COACH)
        return Active()
    }

    override fun inactivate(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus = this

    override fun leave(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus = this

    override fun remove(
        player: Player,
        requestingPlayer: Player,
    ): PlayerStatus = this

    override fun type(): PlayerStatusType = PlayerStatusType.REMOVED
}

@JvmInline
value class Name(
    val value: String,
) {
    init {
        require(value.length in 2..20)
    }
}

data class PlayerAlreadyInGroupException(
    val userId: UserId,
) : RuntimeException("User already in group: ${userId.value}")

data class PlayerNotInvitedInGroupException(
    val userId: UserId,
) : RuntimeException("User not invited in group: ${userId.value}")
