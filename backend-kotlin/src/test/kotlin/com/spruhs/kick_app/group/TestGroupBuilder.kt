package com.spruhs.kick_app.group

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.group.core.adapter.primary.CreateGroupRequest
import com.spruhs.kick_app.group.core.adapter.primary.InviteUserResponse
import com.spruhs.kick_app.group.core.application.ChangeGroupNameCommand
import com.spruhs.kick_app.group.core.application.CreateGroupCommand
import com.spruhs.kick_app.group.core.application.InviteUserCommand
import com.spruhs.kick_app.group.core.application.InviteUserResponseCommand
import com.spruhs.kick_app.group.core.application.UpdatePlayerRoleCommand
import com.spruhs.kick_app.group.core.application.UpdatePlayerStatusCommand
import com.spruhs.kick_app.group.core.domain.GroupAggregate
import com.spruhs.kick_app.group.core.domain.Name
import com.spruhs.kick_app.group.core.domain.Player

import com.spruhs.kick_app.view.core.persistence.GroupDocument
import com.spruhs.kick_app.view.core.persistence.PlayerDocument
import com.spruhs.kick_app.view.core.service.GroupProjection
import com.spruhs.kick_app.view.core.service.PlayerProjection

class TestGroupBuilder() {
    private var groupId = "groupId"
    private var groupName = "groupName"
    private var players = listOf<Player>()
    private var invitedPlayers = listOf("player1", "player2", "player3")

    fun buildDocument(): GroupDocument {
        return GroupDocument(
            id = groupId,
            name = groupName,
            players = players.map { PlayerDocument(it.id.value, it.status.type().name, it.role.name, "") }
        )
    }

    fun buildProjection(): GroupProjection = GroupProjection(
        id = GroupId(groupId),
        name = groupName,
        players = players.map { PlayerProjection(it.id, it.status.type(), it.role, "") }
    )

    fun build(): GroupAggregate {
        return GroupAggregate(groupId).also { group ->
            group.name = Name(groupName)
            group.invitedUsers = invitedPlayers.map { UserId(it) }.toMutableSet()
            group.players = players.toMutableList()
        }
    }

    fun toUpdatePlayerStatusCommand(requestingUser: UserId, newStatus: PlayerStatusType) =
        UpdatePlayerStatusCommand(
            userId = UserId(players.first().id.value),
            updatingUserId = requestingUser,
            groupId = GroupId(groupId),
            newStatus = newStatus
        )

    fun toUpdatePlayerRoleCommand(requestingUser: UserId, newRole: PlayerRole) =
        UpdatePlayerRoleCommand(
            userId = UserId(players.first().id.value),
            updatingUserId = requestingUser,
            groupId = GroupId(groupId),
            newRole = newRole,
        )

    fun toCreateGroupCommand(requestingUser: UserId) =
        CreateGroupCommand(
            userId = requestingUser,
            name = Name(groupName)
        )

    fun toCreateGroupRequest() =
        CreateGroupRequest(
            name = groupName,
        )

    fun toUpdateGroupNameCommand(requestingUser: UserId, newName: String) =
        ChangeGroupNameCommand(
            userId = requestingUser,
            newName = Name(newName),
            groupId = GroupId(this.groupId),
        )

    fun toInviteUserCommand(requestingUser: UserId, invitedUser: UserId) =
        InviteUserCommand(
            inviterId = requestingUser,
            inviteeId = invitedUser,
            groupId = GroupId(this.groupId)
        )

    fun toInvitedUserResponseCommand(requestingUser: UserId, response: Boolean) = InviteUserResponseCommand(
        userId = requestingUser,
        groupId = GroupId(this.groupId),
        response = response
    )

    fun toInvitedUserResponse(response: Boolean) = InviteUserResponse(
        groupId = this.groupId,
        userId = invitedPlayers.first(),
        response = response
    )

    fun withPlayers(players: List<Player>) =
        also { this.players = players }

    fun withName(name: String) = also { this.groupName = name }
    fun withInvitedPlayers(invitedPlayers: List<String>) = also { this.invitedPlayers = invitedPlayers }
    fun withId(id: String) = also { this.groupId = id }

}