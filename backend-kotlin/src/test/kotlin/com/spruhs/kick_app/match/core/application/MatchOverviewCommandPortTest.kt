package com.spruhs.kick_app.match.core.application

import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.es.UnknownEventTypeException
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.core.domain.MatchOverview
import com.spruhs.kick_app.match.core.domain.MatchOverviewEntry
import com.spruhs.kick_app.match.core.domain.MatchState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class MatchOverviewCommandPortTest {

    @MockK
    lateinit var matchOverviewService: MatchOverviewService

    @InjectMockKs
    lateinit var matchOverviewCommandPort: MatchOverviewCommandPort

    @Test
    fun `onEvent should cancel match when MatchCanceledEvent received`(): Unit =
        runBlocking {
            val groupId = GroupId("testGroup")
            val matchId = "testMatch"
            val event = MatchCanceledEvent(aggregateId = matchId, groupId = groupId)
            val overview = MatchOverview(
                groupId = groupId,
                entries = mutableListOf(
                    MatchOverviewEntry(
                        matchId = MatchId(matchId),
                        matchNumber = 1,
                        start = LocalDateTime.now().plusDays(1),
                        state = MatchState.PLANNED,
                    ),
                ),
            )

            coEvery { matchOverviewService.getMatchHistory(groupId) } returns overview
            coEvery { matchOverviewService.save(overview) } returns Unit

            matchOverviewCommandPort.onEvent(event)

            coVerify { matchOverviewService.getMatchHistory(groupId) }
            coVerify { matchOverviewService.save(overview) }
        }

    @Test
    fun `onEvent should enter result when MatchResultEnteredEvent received`(): Unit =
        runBlocking {
            val groupId = GroupId("testGroup")
            val matchId = "testMatch"
            val event = MatchResultEnteredEvent(
                aggregateId = matchId,
                groupId = groupId,
                players = emptyList(),
                start = LocalDateTime.now().minusDays(1),
            )
            val overview = MatchOverview(
                groupId = groupId,
                entries = mutableListOf(
                    MatchOverviewEntry(
                        matchId = MatchId(matchId),
                        matchNumber = 1,
                        start = LocalDateTime.now().minusDays(1),
                        state = MatchState.PLANNED,
                    ),
                ),
            )

            coEvery { matchOverviewService.getMatchHistory(groupId) } returns overview
            coEvery { matchOverviewService.save(overview) } returns Unit

            matchOverviewCommandPort.onEvent(event)

            coVerify { matchOverviewService.getMatchHistory(groupId) }
            coVerify { matchOverviewService.save(overview) }
        }

    @Test
    fun `onEvent should throw UnknownEventTypeException for unknown event`(): Unit =
        runBlocking {
            val unknownEvent = object : BaseEvent("unknownAggregate") {}

            assertFailsWith<UnknownEventTypeException> {
                matchOverviewCommandPort.onEvent(unknownEvent)
            }
        }
}
