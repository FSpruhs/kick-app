package com.spruhs.kick_app.group.core.domain

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.group.api.UserInvitedToGroupEvent
import java.util.UUID

data class Group(
    val id: GroupId,
    val name: Name,
    val players: List<UserId>,
    val invitedUsers: List<UserId>,
    override val domainEvents: List<DomainEvent> = listOf()
) : DomainEventList

fun createGroup(
    name: Name,
    user: UserId
): Group {
    return Group(GroupId(UUID.randomUUID().toString()), name, listOf(user), listOf())
}

fun inviteUserToGroup(
    group: Group,
    inviterId: UserId,
    inviteeId: UserId
): Group {
    if (inviterId !in group.players) {
        throw UserNotAuthorizedException(inviterId)
    }
    if (inviteeId in group.players || inviteeId in group.invitedUsers) {
        throw UserAlreadyInGroupException(inviteeId)
    }
    return group.copy(
        invitedUsers = group.invitedUsers + inviteeId,
        domainEvents = group.domainEvents + UserInvitedToGroupEvent(
            inviteeId = inviteeId.value,
            inviterId = inviterId.value,
            groupId = group.id.value
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