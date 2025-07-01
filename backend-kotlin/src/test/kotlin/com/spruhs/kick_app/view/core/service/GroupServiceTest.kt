package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserImageId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.group.TestGroupBuilder
import com.spruhs.kick_app.group.api.GroupCreatedEvent
import com.spruhs.kick_app.group.api.GroupNameChangedEvent
import com.spruhs.kick_app.group.api.PlayerActivatedEvent
import com.spruhs.kick_app.group.api.PlayerDeactivatedEvent
import com.spruhs.kick_app.group.api.PlayerDowngradedEvent
import com.spruhs.kick_app.group.api.PlayerEnteredGroupEvent
import com.spruhs.kick_app.group.api.PlayerLeavedEvent
import com.spruhs.kick_app.group.api.PlayerPromotedEvent
import com.spruhs.kick_app.group.api.PlayerRemovedEvent
import com.spruhs.kick_app.group.core.domain.Active
import com.spruhs.kick_app.group.core.domain.Inactive
import com.spruhs.kick_app.group.core.domain.Player
import com.spruhs.kick_app.group.core.domain.PlayerNotFoundException
import com.spruhs.kick_app.group.core.domain.Removed
import com.spruhs.kick_app.user.api.UserImageUpdatedEvent
import com.spruhs.kick_app.user.api.UserNickNameChangedEvent
import com.spruhs.kick_app.view.api.UserApi
import com.spruhs.kick_app.view.api.UserData
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class GroupServiceTest {

    @MockK
    lateinit var repository: GroupProjectionRepository

    @MockK
    lateinit var groupNameListRepository: GroupNameListProjectionRepository

    @MockK
    lateinit var userApi: UserApi

    @InjectMockKs
    lateinit var groupService: GroupService

    @Test
    fun `getGroup should return group`(): Unit = runBlocking {
        // Given
        val group = TestGroupBuilder()
            .withPlayers(
                listOf(
                    Player(
                        id = UserId("player1"),
                        status = Active(),
                        role = PlayerRole.COACH,
                    )
                )
            )
            .buildProjection()

        coEvery { repository.findById(group.id) } returns group

        // When
        groupService.getGroup(group.id, group.players.first().id).let { result ->
            // Then
            assertThat(result).isNotNull()
            assertThat(result).isEqualTo(group)
        }
    }

    @Test
    fun `getGroup should throw exception when player not in group`(): Unit = runBlocking {
        // Given
        val group = TestGroupBuilder().buildProjection()

        coEvery { repository.findById(group.id) } returns group

        // When
        assertFailsWith<PlayerNotFoundException> { groupService.getGroup(group.id, UserId("not in group")) }
    }

    @Test
    fun `getGroup should throw exception when player not active`(): Unit = runBlocking {
        // Given
        val group = TestGroupBuilder()
            .withPlayers(
                listOf(
                    Player(
                        id = UserId("player1"),
                        status = Removed(),
                        role = PlayerRole.COACH,
                    )
                )
            )
            .buildProjection()

        coEvery { repository.findById(group.id) } returns group

        // When
        assertFailsWith<UserNotAuthorizedException> { groupService.getGroup(group.id, group.players.first().id) }
    }

    @Test
    fun `getPlayer should return player`(): Unit = runBlocking {
        val group = TestGroupBuilder()
            .withPlayers(
                listOf(
                    Player(
                        id = UserId("player1"),
                        status = Active(),
                        role = PlayerRole.COACH,
                    ),
                    Player(
                        id = UserId("player2"),
                        status = Active(),
                        role = PlayerRole.COACH,
                    )
                )
            )
            .buildProjection()

        coEvery { repository.findById(group.id) } returns group

        // When
        groupService.getPlayer(
            group.id,
            group.players.first().id,
            group.players[1].id
        ).let { result ->
            // Then
            assertThat(result).isNotNull()
            assertThat(result).isEqualTo(group.players.first())
        }
    }

    @Test
    fun `getPlayer should throw exception when player not found`(): Unit = runBlocking {
        val group = TestGroupBuilder().buildProjection()

        coEvery { repository.findById(group.id) } returns group

        // When
        assertFailsWith<PlayerNotFoundException> {
            groupService.getPlayer(
                group.id,
                UserId("not in group"),
                UserId("also not in group")
            )
        }
    }

    @Test
    fun `getPlayer should throw exception when requester not found`(): Unit = runBlocking {
        val group = TestGroupBuilder().withPlayers(
            listOf(
                Player(
                    id = UserId("player1"),
                    status = Active(),
                    role = PlayerRole.COACH,
                )
            )
        ).buildProjection()

        coEvery { repository.findById(group.id) } returns group

        // When
        assertFailsWith<PlayerNotFoundException> {
            groupService.getPlayer(
                group.id,
                group.players.first().id,
                UserId("also not in group")
            )
        }
    }

    @Test
    fun `getPlayer should throw exception when requester not authorized`(): Unit = runBlocking {
        val group = TestGroupBuilder().withPlayers(
            listOf(
                Player(
                    id = UserId("player1"),
                    status = Active(),
                    role = PlayerRole.COACH,
                ),
                Player(
                    id = UserId("player2"),
                    status = Removed(),
                    role = PlayerRole.COACH,
                )
            )
        ).buildProjection()

        coEvery { repository.findById(group.id) } returns group

        // When
        assertFailsWith<UserNotAuthorizedException> {
            groupService.getPlayer(
                group.id,
                group.players.first().id,
                group.players[1].id,
            )
        }
    }

    @Test
    fun `getGroupNameList should return group name list`(): Unit = runBlocking {
        // Given
        val groupNameList = GroupNameListProjection(
            groupId = GroupId("groupId"),
            players = listOf(
                GroupNameListEntry(
                    userId = UserId("player1"),
                    name = "Max",
                    imageUrl = "testUrl"
                )
            )
        )


        coEvery { groupNameListRepository.findByGroupId(groupNameList.groupId) } returns groupNameList

        // When
        groupService.getGroupNameList(groupNameList.groupId, groupNameList.players.first().userId).let { result ->
            // Then
            assertThat(result).isNotNull()
            assertThat(result).isEqualTo(groupNameList.players)
        }
    }

    @Test
    fun `getGroupNameList should throw exception when user not in list`(): Unit = runBlocking {
        // Given
        val groupNameList = GroupNameListProjection(
            groupId = GroupId("groupId"),
            players = emptyList()
        )

        coEvery { groupNameListRepository.findByGroupId(groupNameList.groupId) } returns groupNameList

        // When
        assertFailsWith<UserNotAuthorizedException> {
            groupService.getGroupNameList(
                groupNameList.groupId,
                UserId("not found")
            )
        }
    }

    @Test
    fun `whenEvent should handle user nick name changed event`(): Unit = runBlocking {
        // Given
        val groupNameList = GroupNameListProjection(
            groupId = GroupId("groupId"),
            players = listOf(
                GroupNameListEntry(
                    userId = UserId("player1"),
                    name = "Max",
                )
            )
        )

        val event = UserNickNameChangedEvent(
            aggregateId = groupNameList.players.first().userId.value,
            nickName = "Maximilian"
        )

        coEvery { groupNameListRepository.findByUserId(groupNameList.players.first().userId) } returns listOf(groupNameList)
        coEvery { groupNameListRepository.save(
            groupNameList.copy(
                players = listOf(
                    groupNameList.players.first().copy(name = event.nickName)
                )
            )
        )} returns Unit

        // When
        groupService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle user image updated event`(): Unit = runBlocking {
        // Given
        val groupNameList = GroupNameListProjection(
            groupId = GroupId("groupId"),
            players = listOf(
                GroupNameListEntry(
                    userId = UserId("player1"),
                    name = "Max",
                )
            )
        )

        val event = UserImageUpdatedEvent(
            aggregateId = groupNameList.players.first().userId.value,
            imageId = UserImageId("newImageId")
        )

        coEvery { groupNameListRepository.findByUserId(groupNameList.players.first().userId) } returns listOf(groupNameList)
        coEvery { groupNameListRepository.save(
            groupNameList.copy(
                players = listOf(
                    groupNameList.players.first().copy(imageUrl = event.imageId.value)
                )
            )
        )} returns Unit

        // When
        groupService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle group created`(): Unit = runBlocking {
        // Given
        val group = TestGroupBuilder()
            .withPlayers(
                listOf(
                    Player(
                        id = UserId("player1"),
                        status = Active(),
                        role = PlayerRole.COACH,
                    )
                )
            )
            .buildProjection()

        val event = GroupCreatedEvent(
            aggregateId = group.id.value,
            userId = group.players.first().id,
            name = group.name,
            userStatus = group.players.first().status,
            userRole = group.players.first().role
        )

        coEvery { repository.save(group) } returns Unit
        coEvery { userApi.findUserById(event.userId) } returns UserData(
            id = group.players.first().id,
            email = group.players.first().email,
            nickName = "newNickName",
            imageId = null
        )
        coEvery { groupNameListRepository.save(GroupNameListProjection(
            groupId = group.id,
            players = mutableListOf(GroupNameListEntry(
                userId = group.players.first().id,
                name = "newNickName",
            ))
        )) } returns Unit

        // When
        groupService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handel group name changed event`(): Unit = runBlocking {
        // Given
        val group = TestGroupBuilder().buildProjection()
        val event = GroupNameChangedEvent(
            aggregateId = group.id.value,
            name = "groupNameChanged"
        )

        coEvery { repository.findById(group.id) } returns group
        coEvery { repository.save(group.copy(name = event.name)) } returns Unit

        // When
        groupService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handel player entered event event`(): Unit = runBlocking {
        // Given
        val group = TestGroupBuilder().buildProjection()
        val newPlayer = PlayerProjection(
            id = UserId("player1"),
            status = PlayerStatusType.ACTIVE,
            role = PlayerRole.COACH,
            email = "testMail"
        )
        val event = PlayerEnteredGroupEvent(
            aggregateId = group.id.value,
            userId = newPlayer.id,
            groupName = group.name,
            userStatus = newPlayer.status,
            userRole = newPlayer.role,
        )

        coEvery { repository.findById(group.id) } returns group
        coEvery { userApi.findUserById(newPlayer.id) } returns UserData(
            id = newPlayer.id,
            email = "testMail",
            nickName = "newNickName",
            imageId = null
        )
        coEvery { repository.save(group.copy(players = listOf(newPlayer))) } returns Unit
        coEvery { groupNameListRepository.findByGroupId(group.id) } returns GroupNameListProjection(
            groupId = group.id,
            players = emptyList()
        )
        coEvery { groupNameListRepository.save(GroupNameListProjection(
            groupId = group.id,
            players = mutableListOf(GroupNameListEntry(
                userId = newPlayer.id,
                name = "newNickName",
            ))
        )) } returns Unit

        // When
        groupService.whenEvent(event)
    }
    
    @ParameterizedTest
    @MethodSource("roleData")
    fun `whenEvent should handle player role event`(data: TestPlayerRoleData) = runBlocking {
        // Given
        val group = TestGroupBuilder()
            .withId(data.event.aggregateId)
            .withPlayers(listOf(data.player))
            .buildProjection()
        
        coEvery { repository.findById(group.id) } returns group
        coEvery { repository.save(group.copy(players = listOf(PlayerProjection(
            id = group.players.first().id,
            status = group.players.first().status,
            role = data.expectedRole,
            email = group.players.first().email
        )))) } returns Unit

        // When
        groupService.whenEvent(data.event)
    }

    @ParameterizedTest
    @MethodSource("statusData")
    fun `whenEvent should handle player status event`(data: TestPlayerStatusData) = runBlocking {
        // Given
        val group = TestGroupBuilder()
            .withId(data.event.aggregateId)
            .withPlayers(listOf(data.player))
            .buildProjection()

        coEvery { repository.findById(group.id) } returns group
        coEvery { repository.save(group.copy(players = listOf(PlayerProjection(
            id = group.players.first().id,
            status = data.expectedStatus,
            role = group.players.first().role,
            email = group.players.first().email
        )))) } returns Unit

        // When
        groupService.whenEvent(data.event)
    }
    
    companion object {
        data class TestPlayerRoleData(
            val player: Player,
            val event: BaseEvent,
            val expectedRole: PlayerRole,
        )

        data class TestPlayerStatusData(
            val player: Player,
            val event: BaseEvent,
            val expectedStatus: PlayerStatusType,
        )

        @JvmStatic
        fun statusData() = listOf(
            TestPlayerStatusData(
                player = Player(
                    id = UserId("player1"),
                    status = Inactive(),
                    role = PlayerRole.COACH,
                ),
                event = PlayerActivatedEvent(
                    aggregateId = "groupId",
                    userId = UserId("player1"),
                ),
                expectedStatus = PlayerStatusType.ACTIVE
            ),
            TestPlayerStatusData(
                player = Player(
                    id = UserId("player1"),
                    status = Active(),
                    role = PlayerRole.COACH,
                ),
                event = PlayerDeactivatedEvent(
                    aggregateId = "groupId",
                    userId = UserId("player1"),
                ),
                expectedStatus = PlayerStatusType.INACTIVE
            ),
            TestPlayerStatusData(
                player = Player(
                    id = UserId("player1"),
                    status = Inactive(),
                    role = PlayerRole.COACH,
                ),
                event = PlayerRemovedEvent(
                    aggregateId = "groupId",
                    userId = UserId("player1"),
                    name = "player1",
                ),
                expectedStatus = PlayerStatusType.REMOVED
            ),
            TestPlayerStatusData(
                player = Player(
                    id = UserId("player1"),
                    status = Inactive(),
                    role = PlayerRole.COACH,
                ),
                event = PlayerLeavedEvent(
                    aggregateId = "groupId",
                    userId = UserId("player1"),
                ),
                expectedStatus = PlayerStatusType.LEAVED
            )
        )
        
        @JvmStatic
        fun roleData() = listOf(
            TestPlayerRoleData(
                player = Player(
                    id = UserId("player1"),
                    status = Active(),
                    role = PlayerRole.PLAYER,
                ),
                event = PlayerPromotedEvent(
                    aggregateId = "groupId",
                    userId = UserId("player1"),
                ),
                expectedRole = PlayerRole.COACH
            ),
            TestPlayerRoleData(
                player = Player(
                    id = UserId("player1"),
                    status = Active(),
                    role = PlayerRole.COACH,
                ),
                event = PlayerDowngradedEvent(
                    aggregateId = "groupId",
                    userId = UserId("player1"),
                ),
                expectedRole = PlayerRole.PLAYER
            )
            
        )
    }

}