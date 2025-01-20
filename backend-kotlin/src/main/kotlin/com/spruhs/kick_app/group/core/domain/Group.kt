package com.spruhs.kick_app.group.core.domain

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.group.api.UserInvitedToGroupEvent
import java.util.UUID

class Group(
    private val _id: GroupId,
    private val _name: Name,
    private val _invitedUsers: List<UserId>,
    private val _players: List<UserId>
) : DomainEventList {
    constructor(
        name: Name,
        user: UserId
    ) : this(GroupId(UUID.randomUUID().toString()), name, listOf(), listOf(user))

    override val domainEvents: List<DomainEvent> = mutableListOf()

    fun inviteUser(inviterId: UserId, inviteeId: UserId) {
        if (inviterId !in _players) {
            throw UserNotAuthorizedException(inviterId)
        }
        if (inviteeId in _players || inviteeId in _invitedUsers) {
            throw UserAlreadyInGroupException(inviteeId)
        }
        _invitedUsers + inviteeId
        domainEvents + UserInvitedToGroupEvent(
            inviteeId = inviteeId.value,
            inviterId = inviterId.value,
            groupId = _id.value
        )
    }

    val id: GroupId
        get() = _id

    val name: Name
        get() = _name

    val players: List<UserId>
        get() = _players

    val invitedUsers: List<UserId>
        get() = _invitedUsers
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
}

data class GroupNotFoundException(val groupId: GroupId) : RuntimeException("Group not found with id: ${groupId.value}")
data class UserAlreadyInGroupException(val userId: UserId) : RuntimeException("User already in group: ${userId.value}")