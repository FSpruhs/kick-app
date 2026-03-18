package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.AbstractMongoTest
import com.spruhs.kick_app.common.types.Email
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.group.TestGroupBuilder
import com.spruhs.kick_app.group.core.domain.Player
import com.spruhs.kick_app.view.core.service.PlayerProjection
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GroupProjectionMongoDBTest : AbstractMongoTest() {
    @Autowired
    private lateinit var adapter: GroupProjectionMongoDB

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Test
    fun `save should save group`(): Unit =
        runBlocking {
            // Given
            val group = TestGroupBuilder().buildProjection()

            // When
            adapter.save(group)

            // Then
            adapter.findById(group.id).let { result ->
                assertThat(result).isNotNull()
                assertThat(result?.id).isEqualTo(group.id)
                assertThat(result?.name).isEqualTo(group.name)
                assertThat(result?.players).hasSize(group.players.size)
            }
        }

    @Test
    fun `findByUserId should find all groups with player`(): Unit =
        runBlocking {
            // Given
            val group1 = TestGroupBuilder()
                .withId("grp-1")
                .buildProjection()
                .copy(
                    players = listOf(
                        PlayerProjection(
                            id = UserId("user-1"),
                            status = PlayerStatusType.ACTIVE,
                            role = PlayerRole.PLAYER,
                            email = "")
                    )
                )
            val group2 = TestGroupBuilder().withId("grp-2").buildProjection().copy(
                players = listOf(
                    PlayerProjection(
                        id = UserId("user-2"),
                        status = PlayerStatusType.ACTIVE,
                        role = PlayerRole.PLAYER,
                        email = "")
                )
            )
            val group3 = TestGroupBuilder().withId("grp-3").buildProjection().copy(
                players = listOf(
                    PlayerProjection(
                        id = UserId("user-1"),
                        status = PlayerStatusType.ACTIVE,
                        role = PlayerRole.PLAYER,
                        email = "")
                )
            )

            adapter.save(group1)
            adapter.save(group2)
            adapter.save(group3)

            // When
            adapter.findByUserId(UserId("user-1")).let { result ->

                // Then
                assertThat(result).hasSize(2)
                assertThat(result.map { it.id }).containsExactlyInAnyOrder(group1.id, group3.id)
            }
        }
}
