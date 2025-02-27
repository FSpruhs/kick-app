package com.spruhs.kick_app.group

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.application.CreateGroupCommand
import com.spruhs.kick_app.group.core.application.InviteUserCommand
import com.spruhs.kick_app.group.core.application.InviteUserResponseCommand
import com.spruhs.kick_app.group.core.domain.*

class TestGroupBuilder {
    private var id: String = "Test id"
    private var name: String = "test name"
    private var players: List<Player> = listOf(
        Player(
            id = UserId("test player"),
            status = Active(),
            role = PlayerRole.ADMIN
        )
    )
    private var invitedUsers: List<String> = listOf("test invited user")

    fun withId(id: String) = apply { this.id = id }
    fun withName(name: String) = apply { this.name = name }
    fun withPlayers(players: List<Player>) = apply { this.players = players }
    fun withInvitedUsers(invitedUsers: List<String>) = apply { this.invitedUsers = invitedUsers }

    fun buildCreateGroupCommand(): CreateGroupCommand {
        return CreateGroupCommand(
            players.first().id,
            Name(name)
        )
    }

    fun buildInviteUserCommand(): InviteUserCommand {
        return InviteUserCommand(
            inviterId = players.first().id,
            inviteeId = UserId(invitedUsers.first()),
            groupId = GroupId(id)
        )
    }

    fun buildInviteUserResponseCommand(): InviteUserResponseCommand {
        return InviteUserResponseCommand(
            userId = players.first().id,
            response = true,
            groupId = GroupId(id)
        )
    }

    fun build(): Group {
        return Group(
            GroupId(id),
            Name(name),
            players,
            invitedUsers.map { UserId(it) }
        )
    }
}