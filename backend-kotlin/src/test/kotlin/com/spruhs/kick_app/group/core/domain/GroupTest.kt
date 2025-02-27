package com.spruhs.kick_app.group.core.domain

import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.group.TestGroupBuilder
import com.spruhs.kick_app.group.api.UserEnteredGroupEvent
import com.spruhs.kick_app.group.api.UserInvitedToGroupEvent
import com.spruhs.kick_app.group.api.UserLeavedGroupEvent
import com.spruhs.kick_app.group.api.UserRemovedFromGroupEvent
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
            .withPlayers(listOf(Player(playerId, PlayerStatus.ACTIVE, PlayerRole.ADMIN)))
            .build()

        assertThat(group.isActivePlayer(playerId)).isTrue()
    }

    @ParameterizedTest
    @EnumSource(PlayerStatus::class, names = ["INACTIVE", "LEAVED", "REMOVED"])
    fun `is active player should return false if user is not active player`(playerStatus: PlayerStatus) {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, playerStatus, PlayerRole.ADMIN)))
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
    fun `leave should remove player from group`() {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, PlayerStatus.ACTIVE, PlayerRole.ADMIN)))
            .build()

        group.leave(playerId).let { result ->
            assertThat(result.players).hasSize(1)
            assertThat(result.players.first().status).isEqualTo(PlayerStatus.LEAVED)
            assertThat(result.players.first().role).isEqualTo(PlayerRole.PLAYER)
            assertThat(result.domainEvents).hasSize(1)
            assertThat(result.domainEvents.first()).isInstanceOf(UserLeavedGroupEvent::class.java)
        }
    }

    @Test
    fun `leave should throw exception if player not in group`() {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder().build()

        assertThatThrownBy { group.leave(playerId) }
            .isInstanceOf(PlayerNotFoundException::class.java)
    }

    @Test
    fun `leave should throw exception if was removed`() {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, PlayerStatus.REMOVED, PlayerRole.ADMIN)))
            .build()

        assertThatThrownBy { group.leave(playerId) }
            .isInstanceOf(PlayerNotFoundException::class.java)
    }

    @Test
    fun `remove player should remove player from group`() {
        val playerId = UserId("user-id")
        val requesterId = UserId("requester-id")
        val group = TestGroupBuilder()
            .withPlayers(
                listOf(
                    Player(playerId, PlayerStatus.ACTIVE, PlayerRole.ADMIN),
                    Player(requesterId, PlayerStatus.ACTIVE, PlayerRole.ADMIN)
                )
            )
            .build()

        group.removePlayer(requesterId, playerId).let { result ->
            assertThat(result.players).hasSize(2)
            assertThat(result.players.last().status).isEqualTo(PlayerStatus.REMOVED)
            assertThat(result.players.last().role).isEqualTo(PlayerRole.PLAYER)
            assertThat(result.domainEvents).hasSize(1)
            assertThat(result.domainEvents.first()).isInstanceOf(UserRemovedFromGroupEvent::class.java)
        }
    }

    @Test
    fun `remove player should throw exception if player not in group`() {
        val playerId = UserId("user-id")
        val requesterId = UserId("requester-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(requesterId, PlayerStatus.ACTIVE, PlayerRole.ADMIN)))
            .build()

        assertThatThrownBy { group.removePlayer(requesterId, playerId) }
            .isInstanceOf(PlayerNotFoundException::class.java)
    }

    @Test
    fun `remove player should throw exception if user not authorized`() {
        val playerId = UserId("user-id")
        val requesterId = UserId("requester-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, PlayerStatus.ACTIVE, PlayerRole.PLAYER)))
            .build()

        assertThatThrownBy { group.removePlayer(requesterId, playerId) }
            .isInstanceOf(UserNotAuthorizedException::class.java)
    }

    @Test
    fun `update status should update status`() {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, PlayerStatus.ACTIVE, PlayerRole.ADMIN)))
            .build()

        group.updatePlayerStatus(playerId, PlayerStatus.INACTIVE).let { result ->
            assertThat(result.players).hasSize(1)
            assertThat(result.players.first().status).isEqualTo(PlayerStatus.INACTIVE)
        }
    }

    @ParameterizedTest
    @EnumSource(PlayerStatus::class, names = ["LEAVED", "REMOVED"])
    fun `update status should throw exception if wrong status`(playerStatus: PlayerStatus) {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, PlayerStatus.ACTIVE, PlayerRole.ADMIN)))
            .build()

        assertThatThrownBy { group.updatePlayerStatus(playerId, playerStatus) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `is active admin should return true if active admin`() {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, PlayerStatus.ACTIVE, PlayerRole.ADMIN)))
            .build()

        assertThat(group.isActiveAdmin(playerId)).isTrue()
    }

    @Test
    fun `is active admin should return false if not active admin`() {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, PlayerStatus.ACTIVE, PlayerRole.PLAYER)))
            .build()

        assertThat(group.isActiveAdmin(playerId)).isFalse()
    }

    @Test
    fun `is active admin should return false if not active`() {
        val playerId = UserId("user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, PlayerStatus.INACTIVE, PlayerRole.ADMIN)))
            .build()

        assertThat(group.isActiveAdmin(playerId)).isFalse()
    }

    @Test
    fun `update player should update player`() {
        val playerId = UserId("user-id")
        val requesterId = UserId("requester-id")
        val group = TestGroupBuilder()
            .withPlayers(
                listOf(
                    Player(playerId, PlayerStatus.ACTIVE, PlayerRole.ADMIN),
                    Player(requesterId, PlayerStatus.ACTIVE, PlayerRole.ADMIN)
                )
            )
            .build()


        group.updatePlayerRole(requesterId, playerId, PlayerRole.PLAYER, PlayerStatus.INACTIVE).let { result ->
            assertThat(result.players).hasSize(2)
            assertThat(result.players.last().status).isEqualTo(PlayerStatus.INACTIVE)
            assertThat(result.players.last().role).isEqualTo(PlayerRole.PLAYER)
        }
    }

    @Test
    fun `update player should throw exception if player not in group`() {
        val playerId = UserId("user-id")
        val requesterId = UserId("requester-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(requesterId, PlayerStatus.ACTIVE, PlayerRole.ADMIN)))
            .build()

        assertThatThrownBy { group.updatePlayerRole(requesterId, playerId, PlayerRole.PLAYER, PlayerStatus.INACTIVE) }
            .isInstanceOf(PlayerNotFoundException::class.java)
    }

    @Test
    fun `update player should throw exception if user not authorized`() {
        val playerId = UserId("user-id")
        val requesterId = UserId("requester-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(requesterId, PlayerStatus.ACTIVE, PlayerRole.PLAYER)))
            .build()

        assertThatThrownBy { group.updatePlayerRole(requesterId, playerId, PlayerRole.PLAYER, PlayerStatus.INACTIVE) }
            .isInstanceOf(UserNotAuthorizedException::class.java)
    }

    @ParameterizedTest
    @EnumSource(PlayerStatus::class, names = ["LEAVED", "REMOVED"])
    fun `update player should throw exception if wrong status`(playerStatus: PlayerStatus) {
        val playerId = UserId("user-id")
        val requesterId = UserId("requester-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(playerId, PlayerStatus.ACTIVE, PlayerRole.ADMIN)))
            .build()

        assertThatThrownBy { group.updatePlayerRole(requesterId, playerId, PlayerRole.PLAYER, playerStatus) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `invite user should invite user`() {
        val invitedUserId = UserId("invited-user-id")
        val inviterUserId = UserId("inviter-user-id")
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(inviterUserId, PlayerStatus.ACTIVE, PlayerRole.ADMIN)))
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
            .withPlayers(listOf(Player(invitedUserId, PlayerStatus.ACTIVE, PlayerRole.PLAYER)))
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
            .withPlayers(listOf(Player(inviterUserId, PlayerStatus.ACTIVE, PlayerRole.ADMIN)))
            .withInvitedUsers(listOf(invitedUserId.value))
            .build()

        assertThatThrownBy { group.inviteUser(inviterUserId, invitedUserId) }
            .isInstanceOf(UserAlreadyInGroupException::class.java)
    }

}