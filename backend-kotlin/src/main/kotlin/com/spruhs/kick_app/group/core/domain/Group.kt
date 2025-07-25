package com.spruhs.kick_app.group.core.domain

import com.spruhs.kick_app.common.es.AggregateRoot
import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.es.UnknownEventTypeException
import com.spruhs.kick_app.common.exceptions.PlayerNotFoundException
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.exceptions.UserNotAuthorizedException
import com.spruhs.kick_app.group.api.*
import com.spruhs.kick_app.group.core.application.*

class GroupAggregate(override val aggregateId: String) : AggregateRoot(aggregateId, TYPE) {

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

    private fun handlePlayerRoleEvent(userId: UserId, playerRole: PlayerRole) {
        players.find { it.id == userId }?.let {
            players.remove(it)
            players.add(it.copy(role = playerRole))
        }
    }

    private fun handlePlayerStatusEvent(userId: UserId, status: PlayerStatus) {
        players.find { it.id == userId }?.let {
            players.remove(it)
            players.add(it.copy(status = status))
        }
    }

    fun createGroup(command: CreateGroupCommand) {
        apply(
            GroupCreatedEvent(
                aggregateId = aggregateId,
                userId = command.userId,
                name = command.name.value,
                userStatus = PlayerStatusType.ACTIVE,
                userRole = PlayerRole.COACH,
            )
        )
    }

    fun changeGroupName(command: ChangeGroupNameCommand) {
        require(this.players.find { it.id == command.userId }?.role == PlayerRole.COACH) {
            throw UserNotAuthorizedException(command.userId)
        }

        apply(GroupNameChangedEvent(aggregateId, command.newName.value))
    }

    fun inviteUser(command: InviteUserCommand) {
        this.players.find { it.id == command.inviterId && it.status.type() == PlayerStatusType.ACTIVE }
            ?: throw UserNotAuthorizedException(command.inviterId)

        if (command.inviteeId in this.players.map { it.id }) {
            throw PlayerAlreadyInGroupException(command.inviteeId)
        }

        apply(PlayerInvitedEvent(aggregateId, command.inviteeId, name.value))
    }

    fun inviteUserResponse(command: InviteUserResponseCommand) {
        if (command.userId !in this.invitedUsers) {
            throw PlayerNotInvitedInGroupException(command.userId)
        }

        if (command.response) {
            apply(
                PlayerEnteredGroupEvent(
                    aggregateId = aggregateId,
                    userId = command.userId,
                    groupName = name.value,
                    userStatus = PlayerStatusType.ACTIVE,
                    userRole = PlayerRole.PLAYER
                )
            )
        } else {
            apply(PlayerRejectedGroupEvent(aggregateId, command.userId))
        }
    }

    fun updatePlayerRole(command: UpdatePlayerRoleCommand) {
        players.find { it.id == command.updatingUserId && it.role == PlayerRole.COACH }
            ?: throw UserNotAuthorizedException(command.updatingUserId)

        val player = players.find { it.id == command.userId } ?: throw PlayerNotFoundException(command.userId)

        if (command.newRole == PlayerRole.COACH && player.role != PlayerRole.COACH) {
            apply(PlayerPromotedEvent(aggregateId, command.userId))
        } else if (command.newRole == PlayerRole.PLAYER && player.role != PlayerRole.PLAYER) {
            apply(PlayerDowngradedEvent(aggregateId, command.userId))
        }
    }

    fun updatePlayerStatus(command: UpdatePlayerStatusCommand) {
        val player = players.find { it.id == command.userId } ?: throw PlayerNotFoundException(command.userId)
        val requestingPlayer = if (command.userId == command.updatingUserId) {
            player
        } else {
            players.find { it.id == command.updatingUserId }
                ?: throw PlayerNotFoundException(command.updatingUserId)
        }

        val updatedStatus = when (command.newStatus) {
            PlayerStatusType.ACTIVE -> player.status.activate(player, requestingPlayer)
            PlayerStatusType.INACTIVE -> player.status.inactivate(player, requestingPlayer)
            PlayerStatusType.LEAVED -> player.status.leave(player, requestingPlayer)
            PlayerStatusType.REMOVED -> player.status.remove(player, requestingPlayer)
        }

        when (updatedStatus) {
            is Active -> apply(PlayerActivatedEvent(aggregateId, command.userId))
            is Inactive -> apply(PlayerDeactivatedEvent(aggregateId, command.userId))
            is Leaved -> apply(PlayerLeavedEvent(aggregateId, command.userId))
            is Removed -> apply(PlayerRemovedEvent(aggregateId, command.userId, name.value))
        }
    }

    companion object {
        const val TYPE = "Group"
    }
}

data class Player(
    val id: UserId,
    val status: PlayerStatus,
    val role: PlayerRole
)

interface PlayerStatus {
    fun activate(player: Player, requestingPlayer: Player): PlayerStatus
    fun inactivate(player: Player, requestingPlayer: Player): PlayerStatus
    fun leave(player: Player, requestingPlayer: Player): PlayerStatus
    fun remove(player: Player, requestingPlayer: Player): PlayerStatus
    fun type(): PlayerStatusType
}

class Active : PlayerStatus {
    override fun activate(player: Player, requestingPlayer: Player): PlayerStatus {
        return this
    }

    override fun inactivate(player: Player, requestingPlayer: Player): PlayerStatus {
        return Inactive()
    }

    override fun leave(player: Player, requestingPlayer: Player): PlayerStatus {
        require(player == requestingPlayer)
        return Leaved()
    }

    override fun remove(player: Player, requestingPlayer: Player): PlayerStatus {
        require(player != requestingPlayer)
        require(requestingPlayer.role == PlayerRole.COACH)
        return Removed()
    }

    override fun type(): PlayerStatusType {
        return PlayerStatusType.ACTIVE
    }
}

class Inactive : PlayerStatus {
    override fun activate(player: Player, requestingPlayer: Player): PlayerStatus {
        return Active()
    }

    override fun inactivate(player: Player, requestingPlayer: Player): PlayerStatus {
        return this
    }

    override fun leave(player: Player, requestingPlayer: Player): PlayerStatus {
        require(player == requestingPlayer)
        return Leaved()
    }

    override fun remove(player: Player, requestingPlayer: Player): PlayerStatus {
        require(player != requestingPlayer)
        require(requestingPlayer.role == PlayerRole.COACH)
        return Removed()
    }

    override fun type(): PlayerStatusType {
        return PlayerStatusType.INACTIVE
    }
}

class Leaved : PlayerStatus {
    override fun activate(player: Player, requestingPlayer: Player): PlayerStatus {
        require(player == requestingPlayer)
        return Active()
    }

    override fun inactivate(player: Player, requestingPlayer: Player): PlayerStatus {
        return this
    }

    override fun leave(player: Player, requestingPlayer: Player): PlayerStatus {
        return this
    }

    override fun remove(player: Player, requestingPlayer: Player): PlayerStatus {
        require(player != requestingPlayer)
        require(requestingPlayer.role == PlayerRole.COACH)
        return Removed()
    }

    override fun type(): PlayerStatusType {
        return PlayerStatusType.LEAVED
    }
}

class Removed : PlayerStatus {
    override fun activate(player: Player, requestingPlayer: Player): PlayerStatus {
        require(player != requestingPlayer)
        require(requestingPlayer.role == PlayerRole.COACH)
        return Active()
    }

    override fun inactivate(player: Player, requestingPlayer: Player): PlayerStatus {
        return this
    }

    override fun leave(player: Player, requestingPlayer: Player): PlayerStatus {
        return this
    }

    override fun remove(player: Player, requestingPlayer: Player): PlayerStatus {
        return this
    }

    override fun type(): PlayerStatusType {
        return PlayerStatusType.REMOVED
    }
}

@JvmInline
value class Name(val value: String) {
    init {
        require(value.length in 2..20)
    }
}


data class PlayerAlreadyInGroupException(val userId: UserId) :
    RuntimeException("User already in group: ${userId.value}")

data class PlayerNotInvitedInGroupException(val userId: UserId) :
    RuntimeException("User not invited in group: ${userId.value}")
