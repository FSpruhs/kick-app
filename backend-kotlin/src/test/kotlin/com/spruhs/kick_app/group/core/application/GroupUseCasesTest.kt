package com.spruhs.kick_app.group.core.application

import com.spruhs.kick_app.common.EventPublisher
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.TestGroupBuilder
import com.spruhs.kick_app.group.core.domain.*
import com.spruhs.kick_app.user.api.UserApi
import com.spruhs.kick_app.user.api.UserData
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GroupUseCasesTest {

    @MockK
    lateinit var groupPersistencePort: GroupPersistencePort

    @MockK
    lateinit var eventPublisher: EventPublisher

    @MockK
    lateinit var userApi: UserApi

    @InjectMockKs
    lateinit var useCases: GroupUseCases

    @Test
    fun `create should save group to persistence`() = runBlocking {
        val command = TestGroupBuilder().buildCreateGroupCommand()

        coEvery { groupPersistencePort.save(any()) } just Runs

        useCases.create(command)

        coVerify { groupPersistencePort.save(any()) }
    }

    @Test
    fun `inviteUser should save group to persistence and publish events`() = runBlocking {
        val command = TestGroupBuilder().buildInviteUserCommand()
        val group = TestGroupBuilder().withInvitedUsers(listOf()).build()

        coEvery { groupPersistencePort.findById(command.groupId) } returns group
        coEvery { groupPersistencePort.save(any()) } just Runs
        coEvery { eventPublisher.publishAll(any()) } just Runs

        useCases.inviteUser(command)

        coVerify { groupPersistencePort.save(any()) }
        coVerify { eventPublisher.publishAll(any()) }
    }

    @Test
    fun `get active players`() {
        val group = TestGroupBuilder()
            .withPlayers(listOf(
                Player(UserId("1"), Active(), PlayerRole.PLAYER),
                Player(UserId("2"), Inactive(), PlayerRole.PLAYER),
            ))
            .build()

        every { groupPersistencePort.findById(group.id) } returns group

        useCases.getActivePlayers(group.id).let { result ->
            assertThat(result).containsExactly(UserId("1"))
        }
    }

    @Test
    fun `isActiveMember should return true if user is active member`() {
        val group = TestGroupBuilder()
            .withPlayers(listOf(
                Player(UserId("1"), Active(), PlayerRole.PLAYER),
            ))
            .build()

        every { groupPersistencePort.findById(group.id) } returns group

        useCases.isActiveMember(group.id, UserId("1")).let { result ->
            assertThat(result).isTrue()
        }
    }

    @Test
    fun `isActiveMember should return false if user is not active member`() {
        val group = TestGroupBuilder()
            .withPlayers(listOf(
                Player(UserId("1"), Inactive(), PlayerRole.PLAYER),
            ))
            .build()

        every { groupPersistencePort.findById(group.id) } returns group

        useCases.isActiveMember(group.id, UserId("1")).let { result ->
            assertThat(result).isFalse()
        }
    }

    @Test
    fun `isActiveAdmin should return true if user is active admin`() {
        val group = TestGroupBuilder()
            .withPlayers(listOf(
                Player(UserId("1"), Active(), PlayerRole.ADMIN),
            ))
            .build()

        every { groupPersistencePort.findById(group.id) } returns group

        useCases.isActiveAdmin(group.id, UserId("1")).let { result ->
            assertThat(result).isTrue()
        }
    }

    @Test
    fun `isActiveAdmin should return false if user is not active admin`() {
        val group = TestGroupBuilder()
            .withPlayers(listOf(
                Player(UserId("1"), Active(), PlayerRole.PLAYER),
            ))
            .build()

        every { groupPersistencePort.findById(group.id) } returns group

        useCases.isActiveAdmin(group.id, UserId("1")).let { result ->
            assertThat(result).isFalse()
        }
    }

    @Test
    fun `inviteUserResponse should save group to persistence and publish events`() {
        val command = TestGroupBuilder().buildInviteUserResponseCommand()
        val group = TestGroupBuilder().withInvitedUsers(listOf(command.userId.value)).build()

        every { groupPersistencePort.findById(command.groupId) } returns group
        every { groupPersistencePort.save(any()) } just Runs
        every { eventPublisher.publishAll(any()) } just Runs

        useCases.inviteUserResponse(command)

        verify { groupPersistencePort.save(any()) }
        verify { eventPublisher.publishAll(any()) }
    }

    @Test
    fun `updatePlayer should update status save group to persistence`() {
        val group = TestGroupBuilder().build()
        val command = UpdatePlayerCommand(
            groupId = group.id,
            updatingUserId = group.players.first().id,
            userId = group.players.first().id,
            newStatus = PlayerStatusType.INACTIVE,
        )

        every { groupPersistencePort.findById(command.groupId) } returns group
        every { groupPersistencePort.save(any()) } just Runs
        every { eventPublisher.publishAll(any()) } just Runs

        useCases.updatePlayer(command)

        verify { groupPersistencePort.save(any()) }
        verify { eventPublisher.publishAll(any()) }
    }

    @Test
    fun `getGroupsByPlayer should return groups by player`() {
        val userId = UserId("test user")
        val groups = listOf(
            TestGroupBuilder().withId("1").build(),
            TestGroupBuilder().withId("2").build()
        )

        every { groupPersistencePort.findByPlayer(userId) } returns groups

        useCases.getGroupsByPlayer(userId).let { result ->
            assertThat(result).containsExactlyElementsOf(groups)
        }
    }

    @Test
    fun `getGroupDetails should return group details`() {
        val group = TestGroupBuilder().build()
        val userId = group.players.first().id

        every { groupPersistencePort.findById(group.id) } returns group
        every { userApi.findUsersByIds(group.players.map { it.id }) } returns group.players.map { UserData(it.id, "test name") }

        useCases.getGroupDetails(group.id, userId).let { result ->
            assertThat(result).isNotNull()
            assertThat(result.id).isEqualTo(group.id.value)
            assertThat(result.name).isEqualTo(group.name.value)
            assertThat(result.players).hasSize(1)
            assertThat(result.players.first().id).isEqualTo(group.players.first().id.value)
            assertThat(result.players.first().nickName).isEqualTo("test name")
            assertThat(result.players.first().role).isEqualTo(group.players.first().role)
            assertThat(result.players.first().status).isEqualTo(group.players.first().status)
        }
    }

    @Test
    fun `getGroupDetails should throw exception when group not found`() {
        val groupId = GroupId("test group")
        val userId = UserId("test user")
        every { groupPersistencePort.findById(groupId) } returns null

        assertThatThrownBy {
            useCases.getGroupDetails(groupId, userId)
        }.isInstanceOf(GroupNotFoundException::class.java)
    }
}