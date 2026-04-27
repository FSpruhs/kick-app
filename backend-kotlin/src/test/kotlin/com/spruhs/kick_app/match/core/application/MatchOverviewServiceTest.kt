package com.spruhs.kick_app.match.core.application

import com.spruhs.kick_app.common.es.EventPublisher
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.match.core.domain.MatchOverview
import com.spruhs.kick_app.match.core.domain.MatchOverviewEntry
import com.spruhs.kick_app.match.core.domain.MatchOverviewPersistencePort
import com.spruhs.kick_app.match.core.domain.MatchState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class MatchOverviewServiceTest {
    @MockK
    lateinit var matchOverviewPersistencePort: MatchOverviewPersistencePort

    @MockK
    lateinit var eventPublisher: EventPublisher

    @InjectMockKs
    lateinit var matchOverviewService: MatchOverviewService

    @Test
    fun `getMatchHistory should return existing overview from persistence`(): Unit =
        runBlocking {
            val groupId = GroupId("testGroup")
            val overview =
                MatchOverview(
                    groupId = groupId,
                    entries =
                        mutableListOf(
                            MatchOverviewEntry(
                                matchId = MatchId("match1"),
                                matchNumber = 1,
                                start = LocalDateTime.now().plusDays(1),
                                state = MatchState.PLANNED,
                            ),
                        ),
                )

            coEvery { matchOverviewPersistencePort.getOverview(groupId) } returns overview

            val result = matchOverviewService.getMatchHistory(groupId)

            assertThat(result).isEqualTo(overview)
            assertThat(result.entries).hasSize(1)
        }

    @Test
    fun `getMatchHistory should create new overview when none exists`(): Unit =
        runBlocking {
            val groupId = GroupId("testGroup")

            coEvery { matchOverviewPersistencePort.getOverview(groupId) } returns null

            val result = matchOverviewService.getMatchHistory(groupId)

            assertThat(result.groupId).isEqualTo(groupId)
            assertThat(result.entries).isEmpty()
        }

    @Test
    fun `save should publish events and persist overview`(): Unit =
        runBlocking {
            val groupId = GroupId("testGroup")
            val overview = MatchOverview(groupId = groupId)

            every { eventPublisher.publish(overview.events) } just runs
            coEvery { matchOverviewPersistencePort.save(overview) } just runs

            matchOverviewService.save(overview)

            coVerify { matchOverviewPersistencePort.save(overview) }
            verify { eventPublisher.publish(overview.events) }
        }
}
