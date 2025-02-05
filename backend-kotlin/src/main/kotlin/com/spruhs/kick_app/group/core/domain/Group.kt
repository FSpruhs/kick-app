package com.spruhs.kick_app.group.core.domain

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.group.api.UserEnteredGroupEvent
import com.spruhs.kick_app.group.api.UserInvitedToGroupEvent
import com.spruhs.kick_app.group.api.UserLeavedGroupEvent
import com.spruhs.kick_app.group.api.UserRemovedFromGroupEvent

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

enum class PlayerStatus {
    ACTIVE, INACTIVE, LEAVED, REMOVED
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
    players = listOf(Player(user, PlayerStatus.ACTIVE, PlayerRole.ADMIN)),
    invitedUsers = listOf(),
)

fun Group.inviteUserResponse(
    userId: UserId,
    response: Boolean
): Group {
    if (userId !in this.invitedUsers) {
        throw UserNotInvitedInGroupException(userId)
    }
    if (response) {
        return this.copy(
            players = this.players + Player(userId, PlayerStatus.ACTIVE, PlayerRole.PLAYER),
            invitedUsers = this.invitedUsers - userId,
            domainEvents = this.domainEvents + UserEnteredGroupEvent(
                userId = userId.value,
                groupId = this.id.value
            )
        )
    }
    return this.copy(
        invitedUsers = this.invitedUsers - userId
    )
}

fun Group.leave(userId: UserId): Group {
    val player = players.find { it.id == userId }?.takeIf { it.status != PlayerStatus.REMOVED }
        ?: throw PlayerNotFoundException(userId)

    val updatedPlayer = player.copy(
        status = PlayerStatus.LEAVED,
        role = PlayerRole.PLAYER
    )

    return copy(
        players = players - player + updatedPlayer,
        domainEvents = domainEvents + UserLeavedGroupEvent(
            userId = userId.value,
            groupName = name.value,
            groupId = id.value
        )
    )
}

fun Group.removePlayer(userId: UserId): Group {
    players.find { it.id == userId }?.takeIf { it.role == PlayerRole.ADMIN }
        ?: throw UserNotAuthorizedException(userId)

    val player = players.find { it.id == userId } ?: throw PlayerNotFoundException(userId)

    val updatedPlayer = player.copy(
        status = PlayerStatus.REMOVED,
        role = PlayerRole.PLAYER
    )

    return copy(
        players = players - player + updatedPlayer,
        domainEvents = domainEvents + UserRemovedFromGroupEvent(
            userId = userId.value,
            groupName = name.value,
            groupId = id.value
        )
    )
}

fun Group.updatePlayer(
    requesterId: UserId,
    userId: UserId,
    newRole: PlayerRole,
    newStatus: PlayerStatus
): Group {
    require(newStatus == PlayerStatus.ACTIVE || newStatus == PlayerStatus.INACTIVE)
    players.find { it.id == requesterId }
        ?.takeIf { it.role == PlayerRole.ADMIN }
        ?: throw UserNotAuthorizedException(requesterId)

    val player = players.find { it.id == userId } ?: throw PlayerNotFoundException(userId)

    val updatedPlayer = player.copy(
        role = newRole,
        status = newStatus
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
    fun save(group: Group)
    fun findById(groupId: GroupId): Group?
    fun findByPlayer(userId: UserId): List<Group>
}

data class PlayerNotFoundException(val userId: UserId) : RuntimeException("Player not found with id: ${userId.value}")
data class GroupNotFoundException(val groupId: GroupId) : RuntimeException("Group not found with id: ${groupId.value}")
data class UserAlreadyInGroupException(val userId: UserId) : RuntimeException("User already in group: ${userId.value}")
data class UserNotInvitedInGroupException(val userId: UserId) :
    RuntimeException("User not invited in group: ${userId.value}")