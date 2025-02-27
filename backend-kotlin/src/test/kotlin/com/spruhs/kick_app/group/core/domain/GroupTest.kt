package com.spruhs.kick_app.group.core.domain

import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.group.TestGroupBuilder
import com.spruhs.kick_app.group.api.UserEnteredGroupEvent
import com.spruhs.kick_app.group.api.UserInvitedToGroupEvent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class GroupTest {

    @Test
    fun `create group should create group`() {
        val name = Name("group name")
        val creatingUser = UserId("user-id")

        createGroup(name, creatingUser).let { result ->
            assertThat(result.id.value).isNotBlank()
            assertThat(result.name).isEqualTo(name)
            assertThat(result.players).hasSize(1)
            assertThat(result.players.first().id).isEqualTo(creatingUser)
        }
    }

    @Test
    fun `is active player should return true if user is active player`() {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, Active(), PlayerRole.ADMIN)))
            .build()

        assertThat(group.isActivePlayer(playerId)).isTrue()
    }

    @ParameterizedTest
    @EnumSource(PlayerStatusType::class, names = ["INACTIVE", "LEAVED", "REMOVED"])
    fun `is active player should return false if user is not active player`(playerStatus: PlayerStatusType) {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, playerStatus.toStatus(), PlayerRole.ADMIN)))
            .build()

        assertThat(group.isActivePlayer(playerId)).isFalse()
    }

    @Test
    fun `invite user response should accept user if response is true`() {
        val invitedUserId = UserId("invited-user-id")
        val group = TestGroupBuilder()
            .withInvitedUsers(listOf(invitedUserId.value))
            .build()

        group.inviteUserResponse(invitedUserId, true).let { result ->
            assertThat(result.players).hasSize(2)
            assertThat(result.players.last().id).isEqualTo(invitedUserId)
            assertThat(result.invitedUsers).isEmpty()
            assertThat(result.domainEvents).hasSize(1)
            assertThat(result.domainEvents.first()).isInstanceOf(UserEnteredGroupEvent::class.java)
        }
    }

    @Test
    fun `invite user response should reject user if response is false`() {
        val invitedUserId = UserId("invited-user-id")
        val group = TestGroupBuilder()
            .withInvitedUsers(listOf(invitedUserId.value))
            .build()

        group.inviteUserResponse(invitedUserId, false).let { result ->
            assertThat(result.players).hasSize(1)
            assertThat(result.invitedUsers).isEmpty()
            assertThat(result.domainEvents).isEmpty()
        }
    }

    @Test
    fun `invite user response should throw exception if user not invited`() {
        val invitedUserId = UserId("invited-user-id")
        val group = TestGroupBuilder().build()

        assertThatThrownBy { group.inviteUserResponse(invitedUserId, true) }
            .isInstanceOf(UserNotInvitedInGroupException::class.java)
    }

    @Test
    fun `is active admin should return true if active admin`() {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, Active(), PlayerRole.ADMIN)))
            .build()

        assertThat(group.isActiveAdmin(playerId)).isTrue()
    }

    @Test
    fun `is active admin should return false if not active admin`() {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, Active(), PlayerRole.PLAYER)))
            .build()

        assertThat(group.isActiveAdmin(playerId)).isFalse()
    }

    @Test
    fun `is active admin should return false if not active`() {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, Inactive(), PlayerRole.ADMIN)))
            .build()

        assertThat(group.isActiveAdmin(playerId)).isFalse()
    }

    @Test
    fun `update player should update player role`() {
        val playerId = UserId("user-id")
        val requesterId = UserId("requester-id")
        val group = TestGroupBuilder()
            .withPlayers(
                listOf(
                    Player(playerId, Active(), PlayerRole.ADMIN),
                    Player(requesterId, Active(), PlayerRole.ADMIN)
                )
            )
            .build()


        group.updatePlayerRole(requesterId, playerId, PlayerRole.PLAYER).let { result ->
            assertThat(result.players).hasSize(2)
            assertThat(result.players.last().status.type()).isEqualTo(PlayerStatusType.INACTIVE)
            assertThat(result.players.last().role).isEqualTo(PlayerRole.PLAYER)
        }
    }

    @Test
    fun `update player should throw exception if player not in group`() {
        val playerId = UserId("user-id")
        val requesterId = UserId("requester-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(requesterId, Active(), PlayerRole.ADMIN)))
            .build()

        assertThatThrownBy { group.updatePlayerRole(requesterId, playerId, PlayerRole.PLAYER) }
            .isInstanceOf(PlayerNotFoundException::class.java)
    }

    @Test
    fun `update player should throw exception if user not authorized`() {
        val playerId = UserId("user-id")
        val requesterId = UserId("requester-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(requesterId, Active(), PlayerRole.PLAYER)))
            .build()

        assertThatThrownBy { group.updatePlayerRole(requesterId, playerId, PlayerRole.PLAYER) }
            .isInstanceOf(UserNotAuthorizedException::class.java)
    }

    @Test
    fun `invite user should invite user`() {
        val invitedUserId = UserId("invited-user-id")
        val inviterUserId = UserId("inviter-user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(inviterUserId, Active(), PlayerRole.ADMIN)))
            .withInvitedUsers(emptyList())
            .build()

        group.inviteUser(inviterUserId, invitedUserId).let { result ->
            assertThat(result.invitedUsers).hasSize(1)
            assertThat(result.invitedUsers.first()).isEqualTo(invitedUserId)
            assertThat(result.domainEvents).hasSize(1)
            assertThat(result.domainEvents.first()).isInstanceOf(UserInvitedToGroupEvent::class.java)
        }
    }

    @Test
    fun `invite user should throw exception if user not authorized`() {
        val invitedUserId = UserId("invited-user-id")
        val inviterUserId = UserId("inviter-user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(invitedUserId, Active(), PlayerRole.PLAYER)))
            .withInvitedUsers(emptyList())
            .build()

        assertThatThrownBy { group.inviteUser(inviterUserId, invitedUserId) }
            .isInstanceOf(UserNotAuthorizedException::class.java)
    }

    @Test
    fun `invite user should throw exception if user already invited`() {
        val invitedUserId = UserId("invited-user-id")
        val inviterUserId = UserId("inviter-user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(inviterUserId, Active(), PlayerRole.ADMIN)))
            .withInvitedUsers(listOf(invitedUserId.value))
            .build()

        assertThatThrownBy { group.inviteUser(inviterUserId, invitedUserId) }
            .isInstanceOf(UserAlreadyInGroupException::class.java)
    }

}