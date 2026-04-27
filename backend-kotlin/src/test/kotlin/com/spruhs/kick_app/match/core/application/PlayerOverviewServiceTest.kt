package com.spruhs.kick_app.match.core.application

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.match.api.PlayerOverviewEntry
import com.spruhs.kick_app.match.core.domain.PlayerOverview
import com.spruhs.kick_app.match.core.domain.PlayerOverviewPersistencePort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.junit5.MockKExtension
import io.mockk.runs
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PlayerOverviewServiceTest {

    @MockK
    lateinit var playerOverviewPersistencePort: PlayerOverviewPersistencePort

    @InjectMockKs
    lateinit var playerOverviewService: PlayerOverviewService

    @Test
    fun `getOverview should return existing overview from persistence`(): Unit =
        runBlocking {
            val groupId = GroupId("testGroup")
            val userId = UserId("testUser")
            val overview = PlayerOverview(
                groupId = groupId,
                entries = mutableListOf(PlayerOverviewEntry(userId = userId)),
            )

            coEvery { playerOverviewPersistencePort.getOverview(groupId) } returns overview

            val result = playerOverviewService.getOverview(groupId)

            assertThat(result).isEqualTo(overview)
            assertThat(result.entries).hasSize(1)
        }

    @Test
    fun `getOverview should create new empty overview when none exists`(): Unit =
        runBlocking {
            val groupId = GroupId("testGroup")

            coEvery { playerOverviewPersistencePort.getOverview(groupId) } returns null

            val result = playerOverviewService.getOverview(groupId)

            assertThat(result.groupId).isEqualTo(groupId)
            assertThat(result.entries).isEmpty()
        }

    @Test
    fun `save should delegate to persistence port`(): Unit =
        runBlocking {
            val groupId = GroupId("testGroup")
            val overview = PlayerOverview(groupId = groupId)

            coEvery { playerOverviewPersistencePort.save(overview) } just runs

            playerOverviewService.save(overview)

            coVerify { playerOverviewPersistencePort.save(overview) }
        }

    @Test
    fun `getOverviewEntry should return entry for existing user`(): Unit =
        runBlocking {
            val groupId = GroupId("testGroup")
            val userId = UserId("testUser")
            val entry = PlayerOverviewEntry(userId = userId, attendancePoints = 10)
            val overview = PlayerOverview(
                groupId = groupId,
                entries = mutableListOf(entry),
            )

            coEvery { playerOverviewPersistencePort.getOverview(groupId) } returns overview

            val result = playerOverviewService.getOverviewEntry(groupId, userId)

            assertThat(result).isEqualTo(entry)
            assertThat(result?.attendancePoints).isEqualTo(10)
        }

    @Test
    fun `getOverviewEntry should return null when user not found`(): Unit =
        runBlocking {
            val groupId = GroupId("testGroup")
            val userId = UserId("unknownUser")
            val overview = PlayerOverview(
                groupId = groupId,
                entries = mutableListOf(PlayerOverviewEntry(userId = UserId("otherUser"))),
            )

            coEvery { playerOverviewPersistencePort.getOverview(groupId) } returns overview

            val result = playerOverviewService.getOverviewEntry(groupId, userId)

            assertThat(result).isNull()
        }
}
