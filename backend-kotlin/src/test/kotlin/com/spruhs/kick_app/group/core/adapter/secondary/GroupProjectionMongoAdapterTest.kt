package com.spruhs.kick_app.group.core.adapter.secondary

import com.spruhs.kick_app.AbstractMongoTest
import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UserId
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
import com.spruhs.kick_app.group.core.domain.GroupProjection
import com.spruhs.kick_app.group.core.domain.Inactive
import com.spruhs.kick_app.group.core.domain.Leaved
import com.spruhs.kick_app.group.core.domain.Removed
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

class GroupProjectionMongoAdapterTest : AbstractMongoTest() {

    @Autowired
    private lateinit var adapter: GroupProjectionMongoAdapter

    @Autowired
    private lateinit var repository: GroupRepository

    @Test
    fun `group created event`(): Unit = runBlocking {
        // Given
        val event = GroupCreatedEvent(
            aggregateId = "groupId",
            name = "groupName",
            userId = UserId("userId"),
            userStatus = PlayerStatusType.ACTIVE,
            userRole = PlayerRole.COACH,
        )

        // When
        adapter.whenEvent(event)

        // Then
        adapter.findById(GroupId("groupId")).let { result ->
            assertThat(result).isNotNull
            assertThat(result?.name?.value).isEqualTo("groupName")
            assertThat(result?.players).hasSize(1)
            assertThat(result?.players?.first()?.id?.value).isEqualTo("userId")
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    fun `whenEvent should handle event`(
        data: Triple<GroupDocument, BaseEvent, GroupProjection>
    ): Unit = runBlocking {
        // Given
        val (document, event, expectedGroup) = data
        repository.save(document).awaitSingle()

        // When
        adapter.whenEvent(event)

        // Then
        adapter.findById(GroupId(document.id)).let { result ->
            assertThat(result).isNotNull
            assertThat(result?.name).isEqualTo(expectedGroup.name)
            assertThat(result?.players).hasSize(expectedGroup.players.size)
            assertThat(result?.players).containsExactlyInAnyOrderElementsOf(expectedGroup.players)
        }
    }

    companion object {
        @JvmStatic
        fun data() = listOf(
            Triple(
                TestGroupBuilder().buildDocument(),
                GroupNameChangedEvent(
                    aggregateId = "groupId",
                    name = "newGroupName",
                ),
                TestGroupBuilder()
                    .withName("newGroupName")
                    .buildProjection()
            ),

            Triple(
                TestGroupBuilder().buildDocument(),
                PlayerEnteredGroupEvent(
                    aggregateId = "groupId",
                    groupName = "groupName",
                    userId = UserId("newPlayer"),
                    userStatus = PlayerStatusType.ACTIVE,
                    userRole = PlayerRole.PLAYER
                ),
                TestGroupBuilder()
                    .withPlayer("newPlayer", Active(), PlayerRole.PLAYER)
                    .buildProjection()
            ),

            Triple(
                TestGroupBuilder()
                    .withPlayer("newPlayer", Active(), PlayerRole.PLAYER)
                    .buildDocument(),
                PlayerPromotedEvent(
                    aggregateId = "groupId",
                    userId = UserId("newPlayer")
                ),
                TestGroupBuilder()
                    .withPlayer("newPlayer", Active(), PlayerRole.COACH)
                    .buildProjection()
            ),

            Triple(
                TestGroupBuilder()
                    .withPlayer("newPlayer", Active(), PlayerRole.COACH)
                    .buildDocument(),
                PlayerDowngradedEvent(
                    aggregateId = "groupId",
                    userId = UserId("newPlayer")
                ),
                TestGroupBuilder()
                    .withPlayer("newPlayer", Active(), PlayerRole.PLAYER)
                    .buildProjection()
            ),

            Triple(
                TestGroupBuilder()
                    .withPlayer("newPlayer", Inactive(), PlayerRole.PLAYER)
                    .buildDocument(),
                PlayerActivatedEvent(
                    aggregateId = "groupId",
                    userId = UserId("newPlayer")
                ),
                TestGroupBuilder()
                    .withPlayer("newPlayer", Active(), PlayerRole.PLAYER)
                    .buildProjection()
            ),

            Triple(
                TestGroupBuilder()
                    .withPlayer("newPlayer", Active(), PlayerRole.PLAYER)
                    .buildDocument(),
                PlayerDeactivatedEvent(
                    aggregateId = "groupId",
                    userId = UserId("newPlayer")
                ),
                TestGroupBuilder()
                    .withPlayer("newPlayer", Inactive(), PlayerRole.PLAYER)
                    .buildProjection()
            ),

            Triple(
                TestGroupBuilder()
                    .withPlayer("newPlayer", Active(), PlayerRole.PLAYER)
                    .buildDocument(),
                PlayerRemovedEvent(
                    aggregateId = "groupId",
                    userId = UserId("newPlayer"),
                    name = "groupName"
                ),
                TestGroupBuilder()
                    .withPlayer("newPlayer", Removed(), PlayerRole.PLAYER)
                    .buildProjection()
            ),

            Triple(
                TestGroupBuilder()
                    .withPlayer("newPlayer", Active(), PlayerRole.PLAYER)
                    .buildDocument(),
                PlayerLeavedEvent(
                    aggregateId = "groupId",
                    userId = UserId("newPlayer")
                ),
                TestGroupBuilder()
                    .withPlayer("newPlayer", Leaved(), PlayerRole.PLAYER)
                    .buildProjection()
            )
        )
    }
}

