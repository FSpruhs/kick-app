package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserImageId
import com.spruhs.kick_app.common.UserNotFoundException
import com.spruhs.kick_app.group.api.GroupCreatedEvent
import com.spruhs.kick_app.group.api.GroupNameChangedEvent
import com.spruhs.kick_app.group.api.PlayerActivatedEvent
import com.spruhs.kick_app.group.api.PlayerDeactivatedEvent
import com.spruhs.kick_app.group.api.PlayerDowngradedEvent
import com.spruhs.kick_app.group.api.PlayerLeavedEvent
import com.spruhs.kick_app.group.api.PlayerPromotedEvent
import com.spruhs.kick_app.group.api.PlayerRemovedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.user.TestUserBuilder
import com.spruhs.kick_app.user.api.UserCreatedEvent
import com.spruhs.kick_app.user.api.UserImageUpdatedEvent
import com.spruhs.kick_app.user.api.UserNickNameChangedEvent
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
import java.time.LocalDateTime
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    lateinit var repository: UserProjectionRepository

    @InjectMockKs
    lateinit var userService: UserService

    @Test
    fun `getUser should get user`(): Unit = runBlocking {
        // Given
        val user = TestUserBuilder().buildProjection()

        coEvery { repository.getUser(user.id) } returns user

        // When
        val result = userService.getUser(user.id)

        // Then
        assertThat(result).isEqualTo(user)
    }

    @Test
    fun `getUser should throw UserNotFoundException when user not found`(): Unit = runBlocking {
        // Given
        val user = UserId("1234")

        coEvery { repository.getUser(user) } returns null

        // When
        assertFailsWith<UserNotFoundException> {
            userService.getUser(user)
        }
    }


    @Test
    fun `whenEvent should handle user created`(): Unit = runBlocking {
        // Given
        val user = minimalUserProjection()
        val event = UserCreatedEvent(
            aggregateId = user.id.value,
            email = user.email,
            nickName = user.nickName
        )

        coEvery { repository.save(user) } returns Unit

        // When
        userService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle nick name changed event`(): Unit = runBlocking {
        // Given
        val user = minimalUserProjection()

        val event = UserNickNameChangedEvent(
            aggregateId = user.id.value,
            nickName = "newNickName"
        )

        coEvery { repository.getUser(user.id) } returns user
        coEvery { repository.save(user.copy(nickName = event.nickName)) } returns Unit

        // When
        userService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle user image updated event`(): Unit = runBlocking {
        // Given
        val user = minimalUserProjection()
        val event = UserImageUpdatedEvent(
            aggregateId = user.id.value,
            imageId = UserImageId("newImaGgeId")
        )

        coEvery { repository.getUser(user.id) } returns user
        coEvery { repository.save(user.copy(userImageId = event.imageId)) } returns Unit

        // When
        userService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle group name changed event`(): Unit = runBlocking {
        // Given
        val groupId = GroupId("testGroupId")
        val user = TestUserBuilder()
            .withGroups(
                listOf(
                    UserGroupProjection(
                        id = groupId,
                        name = "oldGroupName",
                        userStatus = PlayerStatusType.ACTIVE,
                        userRole = PlayerRole.PLAYER
                    ),
                    UserGroupProjection(
                        id = GroupId("otherGroupId"),
                        name = "otherOldGroupName",
                        userStatus = PlayerStatusType.ACTIVE,
                        userRole = PlayerRole.PLAYER
                    ),
                )
            )
            .buildProjection()
        val event = GroupNameChangedEvent(
            aggregateId = groupId.value,
            name = "New Group Name",
        )

        coEvery { repository.findByGroupId(groupId) } returns listOf(user)
        coEvery { repository.save(user.copy(groups = user.groups.map {
            if (it.id == groupId) it.copy(name = event.name) else it
        })) } returns Unit

        // When
        userService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle match result entered event with new last match`(): Unit = runBlocking {
        // Given
        val user = TestUserBuilder().buildProjection()
        val event = MatchResultEnteredEvent(
            aggregateId = "match 123",
            groupId = user.groups.first().id,
            start = user.groups.first().lastMatch?.plusHours(1) ?: LocalDateTime.now(),
            players = emptyList(),
        )

        coEvery { repository.findByGroupId(user.groups.first().id) } returns listOf(user)
        coEvery { repository.save(user.copy(groups = user.groups.map {
            if (it.id == event.groupId) it.copy(lastMatch = event.start) else it
        })) } returns Unit

        // When
        userService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle match result entered event with no new last match`(): Unit = runBlocking {
        // Given
        val user = TestUserBuilder().buildProjection()
        val event = MatchResultEnteredEvent(
            aggregateId = "match 123",
            groupId = user.groups.first().id,
            start = user.groups.first().lastMatch?.minusHours(1) ?: LocalDateTime.now(),
            players = emptyList(),
        )

        coEvery { repository.findByGroupId(user.groups.first().id) } returns listOf(user)

        // When
        userService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle match result entered event with first last match`(): Unit = runBlocking {
        // Given
        val user = TestUserBuilder().withGroups(listOf(UserGroupProjection(
            id = GroupId("testGroupId"),
            name = "testGroupName",
            userStatus = PlayerStatusType.ACTIVE,
            userRole = PlayerRole.COACH
        ))).buildProjection()
        val event = MatchResultEnteredEvent(
            aggregateId = "match 123",
            groupId = user.groups.first().id,
            start = user.groups.first().lastMatch?.minusHours(1) ?: LocalDateTime.now(),
            players = emptyList(),
        )

        coEvery { repository.findByGroupId(user.groups.first().id) } returns listOf(user)
        coEvery { repository.save(user.copy(groups = user.groups.map {
            if (it.id == event.groupId) it.copy(lastMatch = event.start) else it
        })) } returns Unit

        // When
        userService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle group created event`():Unit = runBlocking {
        // Given
        val user = minimalUserProjection()
        val event = GroupCreatedEvent(
            aggregateId = "new group id",
            userId = user.id,
            name = "New Group",
            userStatus = PlayerStatusType.ACTIVE,
            userRole = PlayerRole.COACH
        )

        coEvery { repository.getUser(user.id) } returns user
        coEvery { repository.save(user.copy(groups = user.groups + UserGroupProjection(
            id = GroupId(event.aggregateId),
            name = event.name,
            userStatus = event.userStatus,
            userRole = event.userRole,
            lastMatch = null
        )))} returns Unit

        // When
        userService.whenEvent(event)
    }

    @ParameterizedTest
    @MethodSource("statusData")
    fun `whenEvent should handle player status events`(data: TestPlayerStatusData): Unit = runBlocking {
        // Given
        val user = TestUserBuilder().withGroups(listOf(data.group)).buildProjection()

        coEvery { repository.getUser(user.id) } returns user
        coEvery { repository.save(user.copy(groups = data.newGroups)) } returns Unit

        // When
        userService.whenEvent(data.event)
    }

    @ParameterizedTest
    @MethodSource("roleData")
    fun `whenEvent should handle player role events`(data: TestPlayerRoleData): Unit = runBlocking {
        // Given
        val user = TestUserBuilder().withGroups(listOf(data.group)).buildProjection()

        coEvery { repository.getUser(user.id) } returns user
        coEvery { repository.save(user.copy(groups = listOf(user.groups.first().copy(userRole = data.newRole)))) } returns Unit

        // When
        userService.whenEvent(data.event)
    }

    companion object {
        data class TestPlayerStatusData(
            val event: BaseEvent,
            val group: UserGroupProjection,
            val newGroups: List<UserGroupProjection>
        )
        @JvmStatic
        fun statusData() = listOf(
            TestPlayerStatusData(
                event = PlayerActivatedEvent(
                    aggregateId = "testGroupId",
                    userId = UserId("testUserId"),
                ),
                group = UserGroupProjection(
                    id = GroupId("testGroupId"),
                    name = "testGroupName",
                    userStatus = PlayerStatusType.REMOVED,
                    userRole = PlayerRole.PLAYER,
                ),
                newGroups = listOf(UserGroupProjection(
                    id = GroupId("testGroupId"),
                    name = "testGroupName",
                    userStatus = PlayerStatusType.ACTIVE,
                    userRole = PlayerRole.PLAYER,
                )),
            ),
            TestPlayerStatusData(
                event = PlayerDeactivatedEvent(
                    aggregateId = "testGroupId",
                    userId = UserId("testUserId"),
                ),
                group = UserGroupProjection(
                    id = GroupId("testGroupId"),
                    name = "testGroupName",
                    userStatus = PlayerStatusType.REMOVED,
                    userRole = PlayerRole.PLAYER,
                ),
                newGroups = listOf(UserGroupProjection(
                    id = GroupId("testGroupId"),
                    name = "testGroupName",
                    userStatus = PlayerStatusType.INACTIVE,
                    userRole = PlayerRole.PLAYER,
                ))
            ),
            TestPlayerStatusData(
                event = PlayerRemovedEvent(
                    aggregateId = "testGroupId",
                    userId = UserId("testUserId"),
                    name = "player name",
                ),
                group = UserGroupProjection(
                    id = GroupId("testGroupId"),
                    name = "testGroupName",
                    userStatus = PlayerStatusType.ACTIVE,
                    userRole = PlayerRole.PLAYER,
                ),
                newGroups = emptyList()
            ),
            TestPlayerStatusData(
                event = PlayerLeavedEvent(
                    aggregateId = "testGroupId",
                    userId = UserId("testUserId"),
                ),
                group = UserGroupProjection(
                    id = GroupId("testGroupId"),
                    name = "testGroupName",
                    userStatus = PlayerStatusType.REMOVED,
                    userRole = PlayerRole.PLAYER,
                ),
                newGroups = emptyList()
            )
        )

        data class TestPlayerRoleData(
            val event: BaseEvent,
            val group: UserGroupProjection,
            val newRole: PlayerRole
        )

        @JvmStatic
        fun roleData() = listOf(
            TestPlayerRoleData(
                event = PlayerPromotedEvent(
                    aggregateId = "testGroupId",
                    userId = UserId("testUserId"),
                ),
                group = UserGroupProjection(
                    id = GroupId("testGroupId"),
                    name = "testGroupName",
                    userStatus = PlayerStatusType.ACTIVE,
                    userRole = PlayerRole.PLAYER,
                ),
                newRole = PlayerRole.COACH
            ),
            TestPlayerRoleData(
                event = PlayerDowngradedEvent(
                    aggregateId = "testGroupId",
                    userId = UserId("testUserId"),
                ),
                group = UserGroupProjection(
                    id = GroupId("testGroupId"),
                    name = "testGroupName",
                    userStatus = PlayerStatusType.INACTIVE,
                    userRole = PlayerRole.COACH,
                ),
                newRole = PlayerRole.PLAYER)

        )
    }
}

private fun minimalUserProjection(): UserProjection =
    TestUserBuilder()
        .withImageId(null)
        .withGroups(emptyList())
        .buildProjection()