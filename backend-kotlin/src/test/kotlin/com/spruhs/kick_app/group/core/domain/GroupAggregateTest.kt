package com.spruhs.kick_app.group.core.domain

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.group.core.application.ChangeGroupNameCommand
import com.spruhs.kick_app.group.core.application.CreateGroupCommand
import com.spruhs.kick_app.group.core.application.InviteUserCommand
import com.spruhs.kick_app.group.core.application.InviteUserResponseCommand
import com.spruhs.kick_app.group.core.application.UpdatePlayerRoleCommand
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import kotlin.test.Test

class GroupAggregateTest {

    @Test
    fun `inviteUser should add user to invitedUsers`() {
        // Given
        val group = GroupAggregate("groupId")
        group.players.add(Player(
            id = UserId("userId"),
            role = PlayerRole.PLAYER,
            status = Active(),
        ))

        val inviteUserCommand = InviteUserCommand(
            inviterId = UserId("userId"),
            inviteeId = UserId("inviteeId"),
            groupId = GroupId(group.aggregateId)
        )

        // When
        group.inviteUser(inviteUserCommand)

        // Then
        assertThat(group.invitedUsers).containsExactly(UserId("inviteeId"))
    }

    @Test
    fun `inviteUser should throw exception if inviter not in group`() {
        // Given
        val group = GroupAggregate("groupId")

        val inviteUserCommand = InviteUserCommand(
            inviterId = UserId("userId"),
            inviteeId = UserId("inviteeId"),
            groupId = GroupId(group.aggregateId)
        )

        assertThatThrownBy {
            // When
            group.inviteUser(inviteUserCommand)
            // Then
        }.isInstanceOf(UserNotAuthorizedException::class.java)
    }

    @Test
    fun `inviteUser should throw exception if inviter is not active`() {
        // Given
        val group = GroupAggregate("groupId")
        group.players.add(Player(
            id = UserId("userId"),
            role = PlayerRole.PLAYER,
            status = Inactive(),
        ))

        val inviteUserCommand = InviteUserCommand(
            inviterId = UserId("userId"),
            inviteeId = UserId("inviteeId"),
            groupId = GroupId(group.aggregateId)
        )

        assertThatThrownBy {
            // When
            group.inviteUser(inviteUserCommand)
            // Then
        }.isInstanceOf(UserNotAuthorizedException::class.java)
    }

    @Test
    fun `inviteUser should throw exception if user already in group`() {
        // Given
        val group = GroupAggregate("groupId")
        group.players.add(Player(
            id = UserId("userId"),
            role = PlayerRole.PLAYER,
            status = Active(),
        ))
        group.players.add(Player(
            id = UserId("inviteeId"),
            role = PlayerRole.PLAYER,
            status = Active(),
        ))

        val inviteUserCommand = InviteUserCommand(
            inviterId = UserId("userId"),
            inviteeId = UserId("inviteeId"),
            groupId = GroupId(group.aggregateId)
        )

        assertThatThrownBy {
            // When
            group.inviteUser(inviteUserCommand)
            // Then
        }.isInstanceOf(PlayerAlreadyInGroupException::class.java)
    }

    @Test
    fun `createGroup should create a new group`() {
        // Given
        val group = GroupAggregate("groupId")

        val command = CreateGroupCommand(
            userId = UserId("ownerId"),
            name = Name("Test Group")
        )

        // When
        group.createGroup(command)

        // Then
        assertThat(group.aggregateId).isEqualTo("groupId")
        assertThat(group.name).isEqualTo(Name("Test Group"))
        assertThat(group.players).hasSize(1)
        assertThat(group.players.first().id).isEqualTo(UserId("ownerId"))
        assertThat(group.players.first().role).isEqualTo(PlayerRole.COACH)
        assertThat(group.players.first().status.type()).isEqualTo(PlayerStatusType.ACTIVE)
        assertThat(group.invitedUsers).isEmpty()
    }

    @Test
    fun `changeGroupName should change group name`() {
        // Given
        val group = GroupAggregate("groupId")
        group.name = Name("Old Group Name")
        group.players.add(Player(
            id = UserId("userId"),
            role = PlayerRole.COACH,
            status = Active(),
        ))

        val command = ChangeGroupNameCommand(
            userId = UserId("userId"),
            groupId = GroupId(group.aggregateId),
            newName = Name("New Group Name")
        )

        // When
        group.changeGroupName(command)

        // Then
        assertThat(group.name).isEqualTo(Name("New Group Name"))
    }

    @Test
    fun `changeGroupName should throw exception if user is not admin`() {
        // Given
        val group = GroupAggregate("groupId")
        group.name = Name("Old Group Name")
        group.players.add(Player(
            id = UserId("userId"),
            role = PlayerRole.PLAYER,
            status = Active(),
        ))

        val command = ChangeGroupNameCommand(
            userId = UserId("userId"),
            groupId = GroupId(group.aggregateId),
            newName = Name("New Group Name")
        )

        assertThatThrownBy {
            // When
            group.changeGroupName(command)
            // Then
        }.isInstanceOf(UserNotAuthorizedException::class.java)
    }

    @Test
    fun `inviteUserResponse should let player enter group`() {
        // Given
        val group = GroupAggregate("groupId")
        group.invitedUsers.add(UserId("userId"))

        val command = InviteUserResponseCommand(
            userId = UserId("userId"),
            groupId = GroupId(group.aggregateId),
            response = true
        )

        // When
        group.inviteUserResponse(command)

        // Then
        assertThat(group.players).hasSize(1)
        assertThat(group.players.first().id).isEqualTo(UserId("userId"))
        assertThat(group.players.first().role).isEqualTo(PlayerRole.PLAYER)
        assertThat(group.players.first().status.type()).isEqualTo(PlayerStatusType.ACTIVE)
        assertThat(group.invitedUsers).isEmpty()
    }

    @Test
    fun `inviteUserResponse should let player decline group`() {
        // Given
        val group = GroupAggregate("groupId")
        group.invitedUsers.add(UserId("userId"))

        val command = InviteUserResponseCommand(
            userId = UserId("userId"),
            groupId = GroupId(group.aggregateId),
            response = false
        )

        // When
        group.inviteUserResponse(command)

        // Then
        assertThat(group.players).isEmpty()
        assertThat(group.invitedUsers).isEmpty()
    }

    @Test
    fun `inviteUserResponse should throw exception if user is not invited`() {
        // Given
        val group = GroupAggregate("groupId")

        val command = InviteUserResponseCommand(
            userId = UserId("userId"),
            groupId = GroupId(group.aggregateId),
            response = true
        )

        assertThatThrownBy {
            // When
            group.inviteUserResponse(command)
            // Then
        }.isInstanceOf(PlayerNotInvitedInGroupException::class.java)
    }

    @Test
    fun `updatePlayerRole should throw exception when updating player not admin`() {
        // Given
        val group = GroupAggregate("groupId")
        group.players.add(Player(
            id = UserId("updatingUserId"),
            role = PlayerRole.PLAYER,
            status = Active(),
        ))

        val command = UpdatePlayerRoleCommand(
            userId = UserId("userId"),
            groupId = GroupId(group.aggregateId),
            updatingUserId = UserId("updatingUserId"),
            newRole = PlayerRole.COACH,
        )

        assertThatThrownBy {
            // When
            group.updatePlayerRole(command)
            // Then
        }.isInstanceOf(UserNotAuthorizedException::class.java)
    }

    @Test
    fun `updatePlayerRole should throw exception when updated player not in group`() {
        // Given
        val group = GroupAggregate("groupId")
        group.players.add(Player(
            id = UserId("updatingUserId"),
            role = PlayerRole.COACH,
            status = Active(),
        ))

        val command = UpdatePlayerRoleCommand(
            userId = UserId("userId"),
            groupId = GroupId(group.aggregateId),
            updatingUserId = UserId("updatingUserId"),
            newRole = PlayerRole.COACH,
        )

        assertThatThrownBy {
            // When
            group.updatePlayerRole(command)
            // Then
        }.isInstanceOf(PlayerNotFoundException::class.java)
    }

    @Test
    fun `updatePlayerRole should downgrade player role`() {
        // Given
        val group = GroupAggregate("groupId")
        group.players.add(Player(
            id = UserId("updatingUserId"),
            role = PlayerRole.COACH,
            status = Active(),
        ))
        group.players.add(Player(
            id = UserId("userId"),
            role = PlayerRole.COACH,
            status = Active(),
        ))

        val command = UpdatePlayerRoleCommand(
            userId = UserId("userId"),
            groupId = GroupId(group.aggregateId),
            updatingUserId = UserId("updatingUserId"),
            newRole = PlayerRole.PLAYER,
        )

        // When
        group.updatePlayerRole(command)

        // Then
        group.players.find { it.id == command.userId }.let {
            assertThat(it).isNotNull
            assertThat(it?.role).isEqualTo(PlayerRole.PLAYER)
        }
    }

    @Test
    fun `updatePlayerRole should upgrade player role`() {
        // Given
        val group = GroupAggregate("groupId")
        group.players.add(Player(
            id = UserId("updatingUserId"),
            role = PlayerRole.COACH,
            status = Active(),
        ))
        group.players.add(Player(
            id = UserId("userId"),
            role = PlayerRole.PLAYER,
            status = Active(),
        ))

        val command = UpdatePlayerRoleCommand(
            userId = UserId("userId"),
            groupId = GroupId(group.aggregateId),
            updatingUserId = UserId("updatingUserId"),
            newRole = PlayerRole.COACH,
        )

        // When
        group.updatePlayerRole(command)

        // Then
        group.players.find { it.id == command.userId }.let {
            assertThat(it).isNotNull
            assertThat(it?.role).isEqualTo(PlayerRole.COACH)
        }
    }

}