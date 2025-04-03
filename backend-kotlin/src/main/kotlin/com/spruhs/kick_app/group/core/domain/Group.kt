package com.spruhs.kick_app.group.core.domain

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.group.api.*
import com.spruhs.kick_app.group.core.application.*

class GroupAggregate(override val aggregateId: String) : AggregateRoot(aggregateId, TYPE) {

    var name: Name = Name("Default")
    val players: MutableList<Player> = mutableListOf()
    private val invitedUsers: MutableList<UserId> = mutableListOf()

    override fun whenEvent(event: Any) {
        when (event) {
            is GroupCreatedEvent -> handleGroupCreatedEvent(event)
            is GroupNameChangedEvent -> handleGroupNameChangedEvent(event)
            is PlayerInvitedEvent -> handlePlayerInvitedEvent(event)
            is PlayerEnteredGroupEvent -> handlePlayerEnteredGroupEvent(event)
            is PlayerRejectedGroupEvent -> handlePlayerRejectedGroupEvent(event)
            is PlayerPromotedEvent -> handlePlayerRoleEvent(UserId(event.userId), PlayerRole.ADMIN)
            is PlayerDowngradedEvent -> handlePlayerRoleEvent(UserId(event.userId), PlayerRole.PLAYER)
            is PlayerActivatedEvent -> handlePlayerStatusEvent(UserId(event.userId), Active())
            is PlayerDeactivatedEvent -> handlePlayerStatusEvent(UserId(event.userId), Inactive())
            is PlayerRemovedEvent -> handlePlayerStatusEvent(UserId(event.userId), Removed())
            is PlayerLeavedEvent -> handlePlayerStatusEvent(UserId(event.userId), Leaved())

            else -> throw UnknownEventTypeException(event)
        }
    }

    private fun handleGroupCreatedEvent(event: GroupCreatedEvent) {
        name = Name(event.name)
        players.add(Player(UserId(event.userId), Active(), PlayerRole.ADMIN))
    }

    private fun handleGroupNameChangedEvent(event: GroupNameChangedEvent) {
        name = Name(event.name)
    }

    private fun handlePlayerInvitedEvent(event: PlayerInvitedEvent) {
        invitedUsers + UserId(event.userId)
    }

    private fun handlePlayerEnteredGroupEvent(event: PlayerEnteredGroupEvent) {
        players + Player(UserId(event.userId), Active(), PlayerRole.PLAYER)
        invitedUsers - UserId(event.userId)
    }

    private fun handlePlayerRejectedGroupEvent(event: PlayerRejectedGroupEvent) {
        invitedUsers - UserId(event.userId)
    }

    private fun handlePlayerRoleEvent(userId: UserId, playerRole: PlayerRole) {
        players.find { it.id == userId }?.let {
            players - it + it.copy(role = playerRole)
        }
    }

    private fun handlePlayerStatusEvent(userId: UserId, status: PlayerStatus) {
        players.find { it.id == userId }?.let {
            players - it + it.copy(status = status)
        }
    }

    fun createGroup(command: CreateGroupCommand) {
        apply(GroupCreatedEvent(aggregateId, command.userId.value, command.name.value))
    }

    fun changeGroupName(command: ChangeGroupNameCommand) {
        require(this.players.find { it.id == command.userId }?.role == PlayerRole.ADMIN) {
            throw UserNotAuthorizedException(command.userId)
        }

        apply(GroupNameChangedEvent(aggregateId, command.newName.value))
    }

    fun inviteUser(command: InviteUserCommand) {
        if (command.inviterId !in this.players.map { it.id }) {
            throw UserNotAuthorizedException(command.inviterId)
        }
        if (command.inviteeId in this.players.map { it.id } || command.inviteeId in this.invitedUsers) {
            throw PlayerAlreadyInGroupException(command.inviteeId)
        }

        apply(PlayerInvitedEvent(aggregateId, command.inviteeId.value))
    }

    fun inviteUserResponse(command: InviteUserResponseCommand) {
        if (command.userId !in this.invitedUsers) {
            throw PlayerNotInvitedInGroupException(command.userId)
        }

        if (command.response) {
            apply(PlayerEnteredGroupEvent(aggregateId, command.userId.value))
        } else {
            apply(PlayerRejectedGroupEvent(aggregateId, command.userId.value))
        }
    }

    fun updatePlayerRole(command: UpdatePlayerRoleCommand) {
        players.find { it.id == command.updatingUserId }
            ?.takeIf { it.role == PlayerRole.ADMIN }
            ?: throw UserNotAuthorizedException(command.updatingUserId)

        players.find { it.id == command.userId } ?: throw PlayerNotFoundException(command.userId)

        if (command.newRole == PlayerRole.ADMIN) {
            apply(PlayerPromotedEvent(aggregateId, command.userId.value))
        } else if (command.newRole == PlayerRole.PLAYER) {
            apply(PlayerDowngradedEvent(aggregateId, command.userId.value))
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
            is Active -> apply(PlayerActivatedEvent(aggregateId, command.userId.value))
            is Inactive -> apply(PlayerDeactivatedEvent(aggregateId, command.userId.value))
            is Leaved -> apply(PlayerLeavedEvent(aggregateId, command.userId.value))
            is Removed -> apply(PlayerRemovedEvent(aggregateId, command.userId.value))
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

enum class PlayerStatusType {
    ACTIVE, INACTIVE, LEAVED, REMOVED;

    fun toStatus(): PlayerStatus {
        return when (this) {
            ACTIVE -> Active()
            INACTIVE -> Inactive()
            LEAVED -> Leaved()
            REMOVED -> Removed()
        }
    }
}

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
        require(requestingPlayer.role == PlayerRole.ADMIN)
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
        require(requestingPlayer.role == PlayerRole.ADMIN)
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
        require(requestingPlayer.role == PlayerRole.ADMIN)
        return Removed()
    }

    override fun type(): PlayerStatusType {
        return PlayerStatusType.LEAVED
    }
}

class Removed : PlayerStatus {
    override fun activate(player: Player, requestingPlayer: Player): PlayerStatus {
        require(player != requestingPlayer)
        require(requestingPlayer.role == PlayerRole.ADMIN)
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

enum class PlayerRole {
    ADMIN, PLAYER
}

@JvmInline
value class Name(val value: String) {
    init {
        require(value.length in 2..20)
    }
}

interface GroupProjectionPort {
    suspend fun whenEvent(event: BaseEvent)
    suspend fun findById(groupId: GroupId): GroupProjection?
    suspend fun findByPlayer(userId: UserId): List<GroupProjection>
}

data class GroupProjection(
    val id: GroupId,
    val name: Name,
    val players: List<PlayerProjection>,
    val invitedUsers: List<UserId>
) {
    fun isActivePlayer(userId: UserId): Boolean =
        players.any { it.id == userId && it.status == PlayerStatusType.ACTIVE }

    fun isActiveAdmin(userId: UserId): Boolean =
        players.any { it.id == userId && it.role == PlayerRole.ADMIN && it.status == PlayerStatusType.ACTIVE }

}

data class PlayerProjection(
    val id: UserId,
    val status: PlayerStatusType,
    val role: PlayerRole
)

data class PlayerNotFoundException(val userId: UserId) : RuntimeException("Player not found with id: ${userId.value}")
data class GroupNotFoundException(val groupId: GroupId) : RuntimeException("Group not found with id: ${groupId.value}")
data class PlayerAlreadyInGroupException(val userId: UserId) : RuntimeException("User already in group: ${userId.value}")
data class PlayerNotInvitedInGroupException(val userId: UserId) :
    RuntimeException("User not invited in group: ${userId.value}")

