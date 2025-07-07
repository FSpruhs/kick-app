package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.types.PlayerNotFoundException
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.UserNotAuthorizedException
import com.spruhs.kick_app.group.api.GroupCreatedEvent
import com.spruhs.kick_app.group.api.PlayerEnteredGroupEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.MatchTeam
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.api.PlayerResult
import com.spruhs.kick_app.view.TestStatisticBuilder
import com.spruhs.kick_app.view.api.GroupApi
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class StatisticServiceTest {

    @MockK
    lateinit var statisticRepository: StatisticProjectionRepository

    @MockK
    lateinit var resultRepository: ResultProjectionRepository

    @MockK
    lateinit var groupApi: GroupApi

    @InjectMockKs
    lateinit var statisticService: StatisticService

    @Test
    fun `getPlayerStatistics should get statistics`(): Unit = runBlocking {
        // Given
        val statistics = TestStatisticBuilder().build()
        val requestingUser = UserId("requestingUserId")

        coEvery { statisticRepository.findByPlayer(statistics.groupId, statistics.userId) } returns statistics
        coEvery { groupApi.isActiveMember(statistics.groupId, requestingUser) } returns true

        // When
        statisticService.getPlayerStatistics(
            statistics.groupId,
            statistics.userId,
            requestingUser
        ).let { result ->
            // Then
            assertThat(result).isEqualTo(statistics)
        }
    }

    @Test
    fun `getPlayerStatistics should throw exception when requester not authorized`(): Unit = runBlocking {
        // Given
        val requestingUser = UserId("requestingUserId")

        coEvery { groupApi.isActiveMember(GroupId("groupId"), requestingUser) } returns false

        // When
        assertFailsWith<UserNotAuthorizedException> {
            statisticService.getPlayerStatistics(
                GroupId("groupId"),
                UserId("userId"),
                requestingUser
            )
        }
    }

    @Test
    fun `getPlayerStatistics should throw exception when player not found`(): Unit = runBlocking {
        // Given
        val statistics = TestStatisticBuilder().build()
        val requestingUser = UserId("requestingUserId")

        coEvery { statisticRepository.findByPlayer(statistics.groupId, statistics.userId) } returns null
        coEvery { groupApi.isActiveMember(statistics.groupId, requestingUser) } returns true

        // When
        assertFailsWith<PlayerNotFoundException> {
            statisticService.getPlayerStatistics(
                statistics.groupId,
                statistics.userId,
                requestingUser
            )
        }
    }

    @Test
    fun `whenEvent should handle group created event`(): Unit = runBlocking {
        // Given
        val event = GroupCreatedEvent(
            aggregateId = "groupId",
            userId = UserId("userId"),
            name = "Test Group",
            userStatus = PlayerStatusType.ACTIVE,
            userRole = PlayerRole.COACH
        )

        val slot = slot<PlayerStatisticProjection>()

        coEvery { statisticRepository.findByPlayer(GroupId(event.aggregateId), event.userId) } returns null
        coEvery { statisticRepository.save(capture(slot)) } returns Unit

        // When
        statisticService.whenEvent(event)

        // Then
        slot.captured.let { result ->
            assertThat(result.groupId).isEqualTo(GroupId(event.aggregateId))
            assertThat(result.userId).isEqualTo(event.userId)
            assertThat(result.totalMatches).isEqualTo(0)
            assertThat(result.wins).isEqualTo(0)
            assertThat(result.losses).isEqualTo(0)
            assertThat(result.draws).isEqualTo(0)
        }
    }

    @Test
    fun `whenEvent should handle player entered group event`(): Unit = runBlocking {
        // Given
        val event = PlayerEnteredGroupEvent(
            aggregateId = "groupId",
            userId = UserId("userId"),
            userStatus = PlayerStatusType.ACTIVE,
            userRole = PlayerRole.COACH,
            groupName = "Test Group"
        )

        coEvery { statisticRepository.findByPlayer(GroupId(event.aggregateId), event.userId) } returns null
        coEvery { statisticRepository.save(any()) } returns Unit

        // When
        statisticService.whenEvent(event)

        // Then
        coEvery { statisticRepository.save(any()) }
    }

    @Test
    fun `whenEvent should handle player entered group second time event`(): Unit = runBlocking {
        // Given
        val event = PlayerEnteredGroupEvent(
            aggregateId = "groupId",
            userId = UserId("userId"),
            userStatus = PlayerStatusType.ACTIVE,
            userRole = PlayerRole.COACH,
            groupName = "Test Group"
        )

        coEvery {
            statisticRepository.findByPlayer(
                GroupId(event.aggregateId),
                event.userId
            )
        } returns TestStatisticBuilder().build()

        // When
        statisticService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle first result entered event`(): Unit = runBlocking {
        // Given
        val event = MatchResultEnteredEvent(
            aggregateId = "matchId",
            groupId = GroupId("groupId"),
            players = listOf(
                ParticipatingPlayer(
                    userId = UserId("userId1"),
                    playerResult = PlayerResult.WIN,
                    team = MatchTeam.A
                ),
                ParticipatingPlayer(
                    userId = UserId("userId2"),
                    playerResult = PlayerResult.LOSS,
                    team = MatchTeam.B
                ),
                ParticipatingPlayer(
                    userId = UserId("userId3"),
                    playerResult = PlayerResult.DRAW,
                    team = MatchTeam.A
                )
            ),
            start = LocalDateTime.now().minusDays(1),
        )

        coEvery { resultRepository.findByMatchId(MatchId(event.aggregateId)) } returns null
        coEvery { resultRepository.save(any()) } returns Unit
        coEvery {
            statisticRepository.findByPlayer(
                event.groupId,
                event.players[0].userId
            )
        } returns TestStatisticBuilder().withUserId(event.players[0].userId).build()
        coEvery {
            statisticRepository.findByPlayer(
                event.groupId,
                event.players[1].userId
            )
        } returns TestStatisticBuilder().withUserId(event.players[1].userId).build()
        coEvery {
            statisticRepository.findByPlayer(
                event.groupId,
                event.players[2].userId
            )
        } returns TestStatisticBuilder().withUserId(event.players[2].userId).build()
        coEvery {
            statisticRepository.save(
                TestStatisticBuilder().withUserId(event.players[0].userId).withTotalMatches(11).withWins(6).build()
            )
        } returns Unit
        coEvery {
            statisticRepository.save(
                TestStatisticBuilder().withUserId(event.players[1].userId).withTotalMatches(11).withLosses(4).build()
            )
        } returns Unit
        coEvery {
            statisticRepository.save(
                TestStatisticBuilder().withUserId(event.players[2].userId).withTotalMatches(11).withDraws(3).build()
            )
        } returns Unit

        // When
        statisticService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle first result entered with new player event`(): Unit = runBlocking {
        // Given
        val event = MatchResultEnteredEvent(
            aggregateId = "matchId",
            groupId = GroupId("groupId"),
            players = listOf(
                ParticipatingPlayer(
                    userId = UserId("userId1"),
                    playerResult = PlayerResult.WIN,
                    team = MatchTeam.A
                ),
            ),
            start = LocalDateTime.now().minusDays(1),
        )

        val slot = slot<PlayerStatisticProjection>()

        coEvery { resultRepository.findByMatchId(MatchId(event.aggregateId)) } returns null
        coEvery { statisticRepository.findByPlayer(event.groupId, event.players[0].userId) } returns null
        coEvery { statisticRepository.save(capture(slot)) } returns Unit
        coEvery { resultRepository.save(any()) } returns Unit

        // When
        statisticService.whenEvent(event)

        // Then
        slot.captured.let { result ->
            assertThat(result.groupId).isEqualTo(event.groupId)
            assertThat(result.userId).isEqualTo(event.players[0].userId)
            assertThat(result.totalMatches).isEqualTo(1)
            assertThat(result.wins).isEqualTo(1)
            assertThat(result.losses).isEqualTo(0)
            assertThat(result.draws).isEqualTo(0)
        }
    }

    @Test
    fun `whenEvent should handle another result entered event old player not exists`(): Unit = runBlocking {
        // Given
        val event = MatchResultEnteredEvent(
            aggregateId = "matchId",
            groupId = GroupId("groupId"),
            players = listOf(
                ParticipatingPlayer(
                    userId = UserId("userId1"),
                    playerResult = PlayerResult.WIN,
                    team = MatchTeam.A
                ),
                ParticipatingPlayer(
                    userId = UserId("userId2"),
                    playerResult = PlayerResult.LOSS,
                    team = MatchTeam.B
                ),
                ParticipatingPlayer(
                    userId = UserId("userId3"),
                    playerResult = PlayerResult.DRAW,
                    team = MatchTeam.A
                )
            ),
            start = LocalDateTime.now().minusDays(1),
        )
        val result = ResultProjection(
            id = "resultId",
            matchId = MatchId(event.aggregateId),
            players = mapOf()
        )

        coEvery { resultRepository.findByMatchId(MatchId(event.aggregateId)) } returns result
        coEvery { resultRepository.save(any()) } returns Unit
        coEvery {
            statisticRepository.findByPlayer(
                event.groupId,
                event.players[0].userId
            )
        } returns TestStatisticBuilder().withUserId(event.players[0].userId).build()
        coEvery {
            statisticRepository.findByPlayer(
                event.groupId,
                event.players[1].userId
            )
        } returns TestStatisticBuilder().withUserId(event.players[1].userId).build()
        coEvery {
            statisticRepository.findByPlayer(
                event.groupId,
                event.players[2].userId
            )
        } returns TestStatisticBuilder().withUserId(event.players[2].userId).build()
        coEvery {
            statisticRepository.save(
                TestStatisticBuilder().withUserId(event.players[0].userId).withTotalMatches(11).withWins(6).build()
            )
        } returns Unit
        coEvery {
            statisticRepository.save(
                TestStatisticBuilder().withUserId(event.players[1].userId).withTotalMatches(11).withLosses(4).build()
            )
        } returns Unit
        coEvery {
            statisticRepository.save(
                TestStatisticBuilder().withUserId(event.players[2].userId).withTotalMatches(11).withDraws(3).build()
            )
        } returns Unit

        // When
        statisticService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle another result entered event with new player`(): Unit = runBlocking {
        // Given
        val event = MatchResultEnteredEvent(
            aggregateId = "matchId",
            groupId = GroupId("groupId"),
            players = listOf(
                ParticipatingPlayer(
                    userId = UserId("userId1"),
                    playerResult = PlayerResult.WIN,
                    team = MatchTeam.A
                ),
            ),
            start = LocalDateTime.now().minusDays(1),
        )
        val result = ResultProjection(
            id = "resultId",
            matchId = MatchId(event.aggregateId),
            players = mapOf()
        )

        val slot = slot<PlayerStatisticProjection>()

        coEvery { resultRepository.findByMatchId(MatchId(event.aggregateId)) } returns result
        coEvery { statisticRepository.findByPlayer(event.groupId, event.players[0].userId) } returns null
        coEvery { statisticRepository.save(capture(slot)) } returns Unit
        coEvery { resultRepository.save(any()) } returns Unit

        // When
        statisticService.whenEvent(event)

        // Then
        slot.captured.let { result ->
            assertThat(result.groupId).isEqualTo(event.groupId)
            assertThat(result.userId).isEqualTo(event.players[0].userId)
            assertThat(result.totalMatches).isEqualTo(1)
            assertThat(result.wins).isEqualTo(1)
            assertThat(result.losses).isEqualTo(0)
            assertThat(result.draws).isEqualTo(0)
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    fun `whenEvent should handle another result entered event with existing player`(data: TestStatisticData): Unit =
        runBlocking {
            // Given
            val event = MatchResultEnteredEvent(
                aggregateId = "matchId",
                groupId = GroupId("groupId"),
                players = listOf(
                    ParticipatingPlayer(
                        userId = UserId("userId1"),
                        playerResult = data.newResult,
                        team = MatchTeam.A
                    ),
                ),
                start = LocalDateTime.now().minusDays(1),
            )
            val result = ResultProjection(
                id = "resultId",
                matchId = MatchId(event.aggregateId),
                players = mapOf(
                    UserId("userId1") to PlayerResultProjection(data.oldResult, MatchTeam.B)
                )
            )

            val oldPlayer = TestStatisticBuilder()
                .withUserId(event.players[0].userId)
                .withGroupId(event.groupId)
                .withTotalMatches(10)
                .withWins(data.oldWins)
                .withLosses(data.oldLosses)
                .withDraws(data.oldDraws)
                .build()

            val slot = slot<PlayerStatisticProjection>()

            coEvery { resultRepository.findByMatchId(MatchId(event.aggregateId)) } returns result
            coEvery { statisticRepository.findByPlayer(event.groupId, event.players[0].userId) } returns oldPlayer
            coEvery { statisticRepository.save(capture(slot)) } returns Unit
            coEvery { resultRepository.save(any()) } returns Unit

            // When
            statisticService.whenEvent(event)

            // Then
            slot.captured.let { newPlayer ->
                assertThat(newPlayer.groupId).isEqualTo(event.groupId)
                assertThat(newPlayer.userId).isEqualTo(event.players[0].userId)
                assertThat(newPlayer.totalMatches).isEqualTo(10)
                assertThat(newPlayer.wins).isEqualTo(data.expectedWins)
                assertThat(newPlayer.losses).isEqualTo(data.expectedLosses)
                assertThat(newPlayer.draws).isEqualTo(data.expectedDraws)
            }
        }

    @Test
    fun `whenEvent should throw exception another result entered event with new player when result changed`(): Unit =
        runBlocking {
            // Given
            val event = MatchResultEnteredEvent(
                aggregateId = "matchId",
                groupId = GroupId("groupId"),
                players = listOf(
                    ParticipatingPlayer(
                        userId = UserId("userId1"),
                        playerResult = PlayerResult.WIN,
                        team = MatchTeam.A
                    ),
                ),
                start = LocalDateTime.now().minusDays(1),
            )
            val result = ResultProjection(
                id = "resultId",
                matchId = MatchId(event.aggregateId),
                players = mapOf(
                    UserId("userId1") to PlayerResultProjection(PlayerResult.LOSS, MatchTeam.B)
                )
            )

            coEvery { resultRepository.findByMatchId(MatchId(event.aggregateId)) } returns result
            coEvery { statisticRepository.findByPlayer(event.groupId, event.players[0].userId) } returns null

            // When
            assertFailsWith<PlayerNotFoundException> { statisticService.whenEvent(event) }
        }

    @Test
    fun `whenEvent should not save when same result`(): Unit = runBlocking {
            // Given
            val event = MatchResultEnteredEvent(
                aggregateId = "matchId",
                groupId = GroupId("groupId"),
                players = listOf(
                    ParticipatingPlayer(
                        userId = UserId("userId1"),
                        playerResult = PlayerResult.WIN,
                        team = MatchTeam.A
                    ),
                ),
                start = LocalDateTime.now().minusDays(1),
            )
            val result = ResultProjection(
                id = "resultId",
                matchId = MatchId(event.aggregateId),
                players = mapOf(
                    UserId("userId1") to PlayerResultProjection(PlayerResult.WIN, MatchTeam.B)
                )
            )

            coEvery { resultRepository.findByMatchId(MatchId(event.aggregateId)) } returns result
            coEvery { statisticRepository.findByPlayer(event.groupId, event.players[0].userId) } returns TestStatisticBuilder().build()
            coEvery { resultRepository.save(any()) } returns Unit

            // When
            statisticService.whenEvent(event)
        }

    @Test
    fun `whenEvent should delete result from not existing player`(): Unit = runBlocking {
        // Given
        val event = MatchResultEnteredEvent(
            aggregateId = "matchId",
            groupId = GroupId("groupId"),
            players = listOf(),
            start = LocalDateTime.now().minusDays(1),
        )

        val resultProjection = TestStatisticBuilder().build()

        val result = ResultProjection(
            id = "resultId",
            matchId = MatchId(event.aggregateId),
            players = mapOf(
                UserId("userId1") to PlayerResultProjection(PlayerResult.WIN, MatchTeam.A),
                UserId("userId2") to PlayerResultProjection(PlayerResult.LOSS, MatchTeam.A),
                UserId("userId3") to PlayerResultProjection(PlayerResult.DRAW, MatchTeam.B)
            )
        )

        coEvery { resultRepository.findByMatchId(MatchId(event.aggregateId)) } returns result
        coEvery { resultRepository.save(any()) } returns Unit
        coEvery { statisticRepository.findByPlayer(event.groupId, UserId("userId1")) } returns TestStatisticBuilder().build()
        coEvery { statisticRepository.findByPlayer(event.groupId, UserId("userId2")) } returns TestStatisticBuilder().build()
        coEvery { statisticRepository.findByPlayer(event.groupId, UserId("userId3")) } returns TestStatisticBuilder().build()
        coEvery { statisticRepository.save(PlayerStatisticProjection(
            id = resultProjection.id,
            groupId = resultProjection.groupId,
            userId = resultProjection.userId,
            totalMatches = resultProjection.totalMatches - 1,
            wins = resultProjection.wins - 1,
            losses = resultProjection.losses,
            draws = resultProjection.draws
        )) } returns Unit
        coEvery { statisticRepository.save(PlayerStatisticProjection(
            id = resultProjection.id,
            groupId = resultProjection.groupId,
            userId = resultProjection.userId,
            totalMatches = resultProjection.totalMatches - 1,
            wins = resultProjection.wins,
            losses = resultProjection.losses - 1,
            draws = resultProjection.draws
        )) } returns Unit
        coEvery { statisticRepository.save(PlayerStatisticProjection(
            id = resultProjection.id,
            groupId = resultProjection.groupId,
            userId = resultProjection.userId,
            totalMatches = resultProjection.totalMatches - 1,
            wins = resultProjection.wins,
            losses = resultProjection.losses,
            draws = resultProjection.draws - 1
        )) } returns Unit

        // When
        statisticService.whenEvent(event)
    }

    companion object {
        data class TestStatisticData(
            val newResult: PlayerResult,
            val oldResult: PlayerResult,
            val oldWins: Int = 1,
            val oldLosses: Int = 1,
            val oldDraws: Int = 1,
            val expectedWins: Int = 1,
            val expectedLosses: Int = 1,
            val expectedDraws: Int = 1

        )

        @JvmStatic
        fun data() = listOf(
            TestStatisticData(
                newResult = PlayerResult.WIN,
                oldResult = PlayerResult.LOSS,
                oldWins = 5,
                oldLosses = 3,
                expectedWins = 6,
                expectedLosses = 2
            ),
            TestStatisticData(
                newResult = PlayerResult.WIN,
                oldResult = PlayerResult.DRAW,
                oldWins = 2,
                oldDraws = 2,
                expectedWins = 3,
                expectedDraws = 1
            ),
            TestStatisticData(
                newResult = PlayerResult.LOSS,
                oldResult = PlayerResult.WIN,
                oldWins = 5,
                oldLosses = 3,
                expectedWins = 4,
                expectedLosses = 4
            ),
            TestStatisticData(
                newResult = PlayerResult.LOSS,
                oldResult = PlayerResult.DRAW,
                oldLosses = 2,
                oldDraws = 2,
                expectedLosses = 3,
                expectedDraws = 1
            ),
            TestStatisticData(
                newResult = PlayerResult.DRAW,
                oldResult = PlayerResult.LOSS,
                oldLosses = 1,
                oldDraws = 1,
                expectedLosses = 0,
                expectedDraws = 2
            ),
            TestStatisticData(
                newResult = PlayerResult.DRAW,
                oldResult = PlayerResult.WIN,
                oldWins = 1,
                oldDraws = 1,
                expectedWins = 0,
                expectedDraws = 2
            ),
        )
    }
}
