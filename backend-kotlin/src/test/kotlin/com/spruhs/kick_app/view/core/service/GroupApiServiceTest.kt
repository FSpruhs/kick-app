package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.group.TestGroupBuilder
import com.spruhs.kick_app.group.core.domain.Active
import com.spruhs.kick_app.group.core.domain.Inactive
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
class GroupApiServiceTest {

    @MockK
    lateinit var repository: GroupProjectionRepository

    @InjectMockKs
    lateinit var groupApiService: GroupApiService

    @Test
    fun `isActiveMember should return true when member active`(): Unit = runBlocking {
        // Given
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(
                id = UserId("test-user-id"),
                status = Active(),
                role = PlayerRole.PLAYER
            )))
            .buildProjection()

        coEvery { repository.findById(group.id) } returns group

        // When
        groupApiService.isActiveMember(group.id, group.players.first().id).let { result ->
            // Then
            assertThat(result).isTrue()
        }

    }

    @Test
    fun `isActiveMember should return false when member not exists`(): Unit = runBlocking {
        // Given
        val group = TestGroupBuilder().buildProjection()

        coEvery { repository.findById(group.id) } returns group

        // When
        groupApiService.isActiveMember(group.id, UserId("not exists")).let { result ->
            // Then
            assertThat(result).isFalse()
        }
    }

    @Test
    fun `isActiveCoach should return true when member active`(): Unit = runBlocking {
        // Given
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(
                id = UserId("test-user-id"),
                status = Active(),
                role = PlayerRole.COACH
            )))
            .buildProjection()

        coEvery { repository.findById(group.id) } returns group

        // When
        groupApiService.isActiveCoach(group.id, group.players.first().id).let { result ->
            // Then
            assertThat(result).isTrue()
        }

    }

    @Test
    fun `isActiveCoach should return false when member not exists`(): Unit = runBlocking {
        // Given
        val group = TestGroupBuilder().buildProjection()

        coEvery { repository.findById(group.id) } returns group

        // When
        groupApiService.isActiveCoach(group.id, UserId("not exists")).let { result ->
            // Then
            assertThat(result).isFalse()
        }
    }

    @Test
    fun `getActivePlayers should return active player`(): Unit = runBlocking {
        // Given
        val group = TestGroupBuilder()
            .withPlayers(listOf(Player(
                id = UserId("test-user-id-1"),
                status = Active(),
                role = PlayerRole.COACH
            ),
                Player(
                    id = UserId("test-user-id-2"),
                    status = Inactive(),
                    role = PlayerRole.COACH
                )))
            .buildProjection()

        coEvery { repository.findById(group.id) } returns group

        // When
        groupApiService.getActivePlayers(group.id).let { result ->
            // Then
            assertThat(result).containsExactly(UserId("test-user-id-1"))
        }
    }

}