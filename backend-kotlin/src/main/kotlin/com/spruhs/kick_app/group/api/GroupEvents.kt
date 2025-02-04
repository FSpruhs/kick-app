package com.spruhs.kick_app.group.api

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