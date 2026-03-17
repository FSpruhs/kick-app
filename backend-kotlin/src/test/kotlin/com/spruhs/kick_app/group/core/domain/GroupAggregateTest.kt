package com.spruhs.kick_app.group.core.domain

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.exceptions.PlayerNotFoundException
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.exceptions.UserNotAuthorizedException
import com.spruhs.kick_app.common.types.Email
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

        val inviterId = UserId("userId")

        // When
        group.inviteUser(inviterId, UserId("inviteeId"))

        // Then
        assertThat(group.invitedUsers).containsExactly(UserId("inviteeId"))
    }

    @Test
    fun `inviteUser should throw exception if inviter not in group`() {
        // Given
        val group = GroupAggregate("groupId")

        val inviterId = UserId("userId")

        assertThatThrownBy {
            // When
            group.inviteUser(inviterId, UserId("inviteeId"))
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

        val inviterId = UserId("userId")

        assertThatThrownBy {
            // When
            group.inviteUser(inviterId, UserId("inviteeId"))
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

        val inviterId = UserId("userId")

        assertThatThrownBy {
            // When
            group.inviteUser(inviterId, UserId("inviteeId"))
            // Then
        }.isInstanceOf(PlayerAlreadyInGroupException::class.java)
    }

    @Test
    fun `createGroup should create a new group`() {
        // Given
        val group = GroupAggregate("groupId")
        
        val userId = UserId("ownerId")
        val name = Name("Test Group")
        

        // When
        group.createGroup(userId, name)

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

        val userId = UserId("userId")
        val newName = Name("New Group Name")

        // When
        group.changeGroupName(userId, newName)

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

        val userId = UserId("userId")
        val newName = Name("New Group Name")

        assertThatThrownBy {
            // When
            group.changeGroupName(userId, newName)
            // Then
        }.isInstanceOf(UserNotAuthorizedException::class.java)
    }

    @Test
    fun `inviteUserResponse should let player enter group`() {
        // Given
        val group = GroupAggregate("groupId")
        group.invitedUsers.add(UserId("userId"))

        val userId = UserId("userId")
        val response = true

        // When
        group.inviteUserResponse(userId, response)

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

        val userId = UserId("userId")
        val response = false

        // When
        group.inviteUserResponse(userId, response)

        // Then
        assertThat(group.players).isEmpty()
        assertThat(group.invitedUsers).isEmpty()
    }

    @Test
    fun `inviteUserResponse should throw exception if user is not invited`() {
        // Given
        val group = GroupAggregate("groupId")

        val userId = UserId("userId")
        val response = true

        assertThatThrownBy {
            // When
            group.inviteUserResponse(userId, response)
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

        val userId = UserId("userId")
        val updatingUserId = UserId("updatingUserId")
        val newRole = PlayerRole.COACH

        assertThatThrownBy {
            // When
            group.updatePlayerRole(userId, updatingUserId, newRole)
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

        val userId = UserId("userId")
        val updatingUserId = UserId("updatingUserId")
        val newRole = PlayerRole.COACH

        assertThatThrownBy {
            // When
            group.updatePlayerRole(userId, updatingUserId, newRole)
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

        val userId = UserId("userId")
        val updatingUserId = UserId("updatingUserId")
        val newRole = PlayerRole.PLAYER


        // When
        group.updatePlayerRole(userId, updatingUserId, newRole)

        // Then
        group.players.find { it.id == userId }.let {
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


        val userId = UserId("userId")
        val updatingUserId = UserId("updatingUserId")
        val newRole = PlayerRole.COACH


        // When
        group.updatePlayerRole(userId, updatingUserId, newRole)

        // Then
        group.players.find { it.id == userId }.let {
            assertThat(it).isNotNull
            assertThat(it?.role).isEqualTo(PlayerRole.COACH)
        }
    }
}