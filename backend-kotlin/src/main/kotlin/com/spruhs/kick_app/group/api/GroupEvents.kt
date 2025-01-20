package com.spruhs.kick_app.group.api

import com.spruhs.kick_app.common.DomainEvent

data class UserInvitedToGroupEvent(
    val inviterId: String,
    val inviteeId: String,
    val groupId: String,
) : DomainEvent {
    override fun eventVersion(): Int = 1
}