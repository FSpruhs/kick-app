package com.spruhs.kick_app.group

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.application.CreateGroupCommand
import com.spruhs.kick_app.group.core.application.InviteUserCommand
import com.spruhs.kick_app.group.core.domain.Group
import com.spruhs.kick_app.group.core.domain.Name

class TestGroupBuilder {
    private var id: String = "Test id"
    private var name: String = "test name"
    private var players: List<String> = listOf("test player")
    private var invitedUsers: List<String> = listOf("test invited user")

    fun withId(id: String) = apply { this.id = id }
    fun withName(name: String) = apply { this.name = name }
    fun withPlayers(players: List<String>) = apply { this.players = players }
    fun withInvitedUsers(invitedUsers: List<String>) = apply { this.invitedUsers = invitedUsers }

    fun buildCreateGroupCommand(): CreateGroupCommand {
        return CreateGroupCommand(
            UserId(players.first()),
            Name(name)
        )
    }

    fun buildInviteUserCommand(): InviteUserCommand {
        return InviteUserCommand(
            inviterId = UserId(players.first()),
            inviteeId = UserId(invitedUsers.first()),
            groupId = GroupId(id)
        )
    }

    fun build(): Group {
        return Group(
            GroupId(id),
            Name(name),
            players.map { UserId(it) },
            invitedUsers.map { UserId(it) }
        )
    }
}