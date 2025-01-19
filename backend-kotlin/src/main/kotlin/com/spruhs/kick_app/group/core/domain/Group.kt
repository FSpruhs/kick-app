package com.spruhs.kick_app.group.core.domain

import java.util.UUID

class Group(
    private val _id: GroupId,
    private val _name: Name,
    private val _invitedUsers: List<UserId>,
    private val _players: List<UserId>
) {
    constructor(
        name: Name,
        user: UserId
    ) : this(GroupId(UUID.randomUUID().toString()), name, listOf(), listOf(user))

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
value class GroupId(val value: String) {
    init {
        require(value.isNotBlank())
    }
}

@JvmInline
value class Name(val value: String) {
    init {
        require(value.length in 2..20)
    }
}

@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank())
    }
}

interface GroupPersistencePort {
    fun save(group: Group)
}