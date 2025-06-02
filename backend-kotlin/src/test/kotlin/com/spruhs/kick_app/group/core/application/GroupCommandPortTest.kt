package com.spruhs.kick_app.group.core.application

import com.spruhs.kick_app.common.AggregateStore
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.domain.Active
import com.spruhs.kick_app.group.core.domain.GroupAggregate
import com.spruhs.kick_app.group.core.domain.Name
import com.spruhs.kick_app.group.core.domain.Player
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GroupCommandPortTest {

    @MockK
    lateinit var aggregateStore: AggregateStore

    @InjectMockKs
    lateinit var groupCommandPort: GroupCommandPort

    @Test
    fun `test create group should return group`(): Unit = runBlocking {
        // Given
        val command = CreateGroupCommand(
            userId = UserId("userId"),
            name = Name("groupName")
        )
        coEvery { aggregateStore.save(any()) } returns Unit

        // When
        val result = groupCommandPort.createGroup(command)

        // Then
        assertThat(result.name.value).isEqualTo("groupName")
    }

    @Test
    fun `test change group name should return group`(): Unit = runBlocking {
        // Given
        val command = ChangeGroupNameCommand(
            userId = UserId("userId"),
            groupId = GroupId("groupId"),
            newName = Name("newGroupName"
            ))
        val group = GroupAggregate(command.userId.value)
        group.name = Name("oldGroupName")
        group.players = mutableListOf(Player(
            id = command.userId,
            role = PlayerRole.COACH,
            status = Active(),
        ))

        coEvery {
            aggregateStore.load(
                command.groupId.value,
                GroupAggregate::class.java
            )
        } returns group
        coEvery { aggregateStore.save(any()) } returns Unit

        // When
        groupCommandPort.changeGroupName(command)

        // Then
        assertThat(group.name).isEqualTo(command.newName)
    }

    @Test
    fun `invite user should invite user`(): Unit = runBlocking {
        // Given
        val command = InviteUserCommand(
            inviterId = UserId("inviterId"),
            inviteeId = UserId("inviteeId"),
            groupId = GroupId("groupId")
        )
        val group = GroupAggregate(command.inviterId.value)
        group.players = mutableListOf(Player(
            id = command.inviterId,
            role = PlayerRole.COACH,
            status = Active(),
        ))

        coEvery {
            aggregateStore.load(
                command.groupId.value,
                GroupAggregate::class.java
            )
        } returns group
        coEvery { aggregateStore.save(any()) } returns Unit

        // When
        groupCommandPort.inviteUser(command)

        // Then
        assertThat(group.invitedUsers).containsExactlyInAnyOrder(command.inviteeId)
    }

    @Test
    fun `update player role should update player role`(): Unit = runBlocking {
        // Given
        val command = UpdatePlayerRoleCommand(
            userId = UserId("userId"),
            updatingUserId = UserId("updatingUserId"),
            groupId = GroupId("groupId"),
            newRole = PlayerRole.COACH,
        )
        val group = GroupAggregate(command.userId.value)
        group.players = mutableListOf(Player(
            id = command.userId,
            role = PlayerRole.PLAYER,
            status = Active(),
        ),
            Player(
                id = command.updatingUserId,
                role = PlayerRole.COACH,
                status = Active(),
            )
        )

        coEvery {
            aggregateStore.load(
                command.groupId.value,
                GroupAggregate::class.java
            )
        } returns group
        coEvery { aggregateStore.save(any()) } returns Unit

        // When
        groupCommandPort.updatePlayerRole(command)

        // Then
        assertThat(group.players[0].role).isEqualTo(command.newRole)
    }

    @Test
    fun `update player status should update player status`(): Unit = runBlocking {
        // Given
        val command = UpdatePlayerStatusCommand(
            userId = UserId("userId"),
            updatingUserId = UserId("updatingUserId"),
            groupId = GroupId("groupId"),
            newStatus = PlayerStatusType.REMOVED,
        )
        val group = GroupAggregate(command.userId.value)
        group.players = mutableListOf(Player(
            id = command.userId,
            role = PlayerRole.PLAYER,
            status = Active(),
        ),
            Player(
                id = command.updatingUserId,
                role = PlayerRole.COACH,
                status = Active(),
            )
        )

        coEvery {
            aggregateStore.load(
                command.groupId.value,
                GroupAggregate::class.java
            )
        } returns group
        coEvery { aggregateStore.save(any()) } returns Unit

        // When
        groupCommandPort.updatePlayerStatus(command)

        // Then
        assertThat(group.players[1].status.type()).isEqualTo(command.newStatus)
    }

    @Test
    fun `invite user response should handle user response`(): Unit = runBlocking {
        // Given
        val command = InviteUserResponseCommand(
            userId = UserId("userId"),
            groupId = GroupId("groupId"),
            response = true
        )
        val group = GroupAggregate(command.userId.value)
        group.invitedUsers = mutableSetOf(command.userId)

        coEvery {
            aggregateStore.load(
                command.groupId.value,
                GroupAggregate::class.java
            )
        } returns group

        coEvery { aggregateStore.save(any()) } returns Unit

        // When
        groupCommandPort.inviteUserResponse(command)

        // Then
        assertThat(group.players).hasSize(1)
    }
}