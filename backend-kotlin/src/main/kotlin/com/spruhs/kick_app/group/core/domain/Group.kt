package com.spruhs.kick_app.group.core.domain

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.group.api.*
import com.spruhs.kick_app.group.core.application.*

data class Group(
    val id: GroupId,
    val name: Name,
    val players: List<Player>,
    val invitedUsers: List<UserId>,
    override val domainEvents: List<DomainEvent> = listOf()
) : DomainEventList

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

fun createGroup(
    name: Name,
    user: UserId
): Group = Group(
    id = GroupId(generateId()),
    name = name,
    players = listOf(Player(user, Active(), PlayerRole.ADMIN)),
    invitedUsers = listOf(),
)

fun Group.isActivePlayer(userId: UserId): Boolean =
    players.any { it.id == userId && it.status.type() == PlayerStatusType.ACTIVE }

fun Group.inviteUserResponse(
    userId: UserId,
    response: Boolean
): Group {
    if (userId !in this.invitedUsers) {
        throw UserNotInvitedInGroupException(userId)
    }
    return if (response) acceptUser(userId) else rejectUser(userId)
}

private fun Group.acceptUser(userId: UserId): Group = copy(
    players = players + Player(userId, Active(), PlayerRole.PLAYER),
    invitedUsers = invitedUsers - userId,
    domainEvents = domainEvents + UserEnteredGroupEvent(userId.value, id.value)
)

private fun Group.rejectUser(userId: UserId): Group = copy(
    invitedUsers = invitedUsers - userId
)

fun Group.updatePlayerStatus(
    userId: UserId,
    requestingUserId: UserId,
    newStatus: PlayerStatusType
): Group {
    val player = players.find { it.id == userId } ?: throw PlayerNotFoundException(userId)
    val requestingPlayer = if (userId == requestingUserId) {
        player
    } else {
        players.find { it.id == requestingUserId } ?: throw PlayerNotFoundException(requestingUserId)
    }

    val updatedStatus = when (newStatus) {
        PlayerStatusType.ACTIVE -> player.status.activate(player, requestingPlayer)
        PlayerStatusType.INACTIVE -> player.status.inactivate(player, requestingPlayer)
        PlayerStatusType.LEAVED -> player.status.leave(player, requestingPlayer)
        PlayerStatusType.REMOVED -> player.status.remove(player, requestingPlayer)
    }

    return if (updatedStatus == player.status) {
        this
    } else {
        val updatedPlayer = player.copy(status = updatedStatus)

        this.copy(
            players = players - player + updatedPlayer,
            domainEvents = domainEvents + PlayerStatusUpdated(
                userId = player.id.value,
                groupId = this.id.value,
                groupName = this.name.value,
                newStatus = updatedPlayer.status.type().toString()
            )
        )
    }
}

fun Group.isActiveAdmin(userId: UserId): Boolean =
    players.any { it.id == userId && it.role == PlayerRole.ADMIN && it.status.type() == PlayerStatusType.ACTIVE }

fun Group.updatePlayerRole(
    requesterId: UserId,
    userId: UserId,
    newRole: PlayerRole,
): Group {
    players.find { it.id == requesterId }
        ?.takeIf { it.role == PlayerRole.ADMIN }
        ?: throw UserNotAuthorizedException(requesterId)

    val player = players.find { it.id == userId } ?: throw PlayerNotFoundException(userId)

    val updatedPlayer = player.copy(
        role = newRole,
    )

    return copy(players = players - player + updatedPlayer)
}

fun Group.inviteUser(
    inviterId: UserId,
    inviteeId: UserId
): Group {
    if (inviterId !in this.players.map { it.id }) {
        throw UserNotAuthorizedException(inviterId)
    }
    if (inviteeId in this.players.map { it.id } || inviteeId in this.invitedUsers) {
        throw UserAlreadyInGroupException(inviteeId)
    }
    return this.copy(
        invitedUsers = this.invitedUsers + inviteeId,
        domainEvents = this.domainEvents + UserInvitedToGroupEvent(
            inviteeId = inviteeId.value,
            groupName = this.name.value,
            groupId = this.id.value
        )
    )
}

@JvmInline
value class Name(val value: String) {
    init {
        require(value.length in 2..20)
    }
}

interface GroupPersistencePort {
    suspend fun save(group: Group)
    suspend fun findById(groupId: GroupId): Group?
    suspend fun findByPlayer(userId: UserId): List<Group>
}

data class PlayerNotFoundException(val userId: UserId) : RuntimeException("Player not found with id: ${userId.value}")
data class GroupNotFoundException(val groupId: GroupId) : RuntimeException("Group not found with id: ${groupId.value}")
data class UserAlreadyInGroupException(val userId: UserId) : RuntimeException("User already in group: ${userId.value}")
data class UserNotInvitedInGroupException(val userId: UserId) :
    RuntimeException("User not invited in group: ${userId.value}")

class GroupAggregate(override val aggregateId: String) : AggregateRoot(aggregateId, TYPE) {

    var name: Name = Name("Default")
    val players: MutableList<Player> = mutableListOf()
    val invitedUsers: MutableList<UserId> = mutableListOf()

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
        apply(GroupCreatedEvent(aggregateId, command.name.value))
    }

    fun changeGroupName(command: ChangeGroupNameCommand) {
        apply(GroupNameChangedEvent(aggregateId, command.newName.value))
    }

    fun inviteUser(command: InviteUserCommand) {
        if (command.inviterId !in this.players.map { it.id }) {
            throw UserNotAuthorizedException(command.inviterId)
        }
        if (command.inviteeId in this.players.map { it.id } || command.inviteeId in this.invitedUsers) {
            throw UserAlreadyInGroupException(command.inviteeId)
        }

        apply(PlayerInvitedEvent(aggregateId, command.inviteeId.value))
    }

    fun inviteUserResponse(command: InviteUserResponseCommand) {
        if (command.userId !in this.invitedUsers) {
            throw UserNotInvitedInGroupException(command.userId)
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
