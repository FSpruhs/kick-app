package com.spruhs.kick_app.group.core.domain

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.group.api.UserInvitedToGroupEvent
import java.util.UUID

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
    id = GroupId(UUID.randomUUID().toString()),
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
        )
    }
    return this.copy(
        invitedUsers = this.invitedUsers - userId
    )
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

data class GroupNotFoundException(val groupId: GroupId) : RuntimeException("Group not found with id: ${groupId.value}")
data class UserAlreadyInGroupException(val userId: UserId) : RuntimeException("User already in group: ${userId.value}")
data class UserNotInvitedInGroupException(val userId: UserId) :
    RuntimeException("User not invited in group: ${userId.value}")