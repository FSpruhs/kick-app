package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.UserNotAuthorizedException
import com.spruhs.kick_app.match.TestMatchBuilder
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.match.core.domain.MatchNotFoundException
import com.spruhs.kick_app.view.api.GroupApi
import com.spruhs.kick_app.view.api.UserApi
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class MatchServiceTest {

    @MockK
    lateinit var repository: MatchProjectionRepository

    @MockK
    lateinit var groupApi: GroupApi

    @MockK
    lateinit var userApi: UserApi

    @InjectMockKs
    lateinit var matchService: MatchService

    @Test
    fun `getMatch should get match`(): Unit = runBlocking {
        // Given
        val match = TestMatchBuilder().toProjection()
        val userId = UserId("test-user")

        coEvery { repository.findById(match.id) } returns match
        coEvery { groupApi.isActiveMember(match.groupId, userId) } returns true

        // When
        matchService.getMatch(match.id, userId).let { result ->
            // Then
            assertThat(result).isNotNull()
            assertThat(result.id).isEqualTo(match.id)
            assertThat(result.groupId).isEqualTo(match.groupId)
            assertThat(result.start.toLocalDate()).isEqualTo(match.start.toLocalDate())
            assertThat(result.playground).isEqualTo(match.playground)
            assertThat(result.maxPlayer).isEqualTo(match.maxPlayer)
            assertThat(result.minPlayer).isEqualTo(match.minPlayer)
            assertThat(result.isCanceled).isEqualTo(match.isCanceled)
            assertThat(result.cadrePlayers).isEqualTo(match.cadrePlayers)
            assertThat(result.deregisteredPlayers).isEqualTo(match.deregisteredPlayers)
            assertThat(result.waitingBenchPlayers).isEqualTo(match.waitingBenchPlayers)
            assertThat(result.result).isEqualTo(match.result)
        }
    }

    @Test
    fun `getMatch should throw exception when match not exists`(): Unit = runBlocking {
        // Given
        val matchId = MatchId("non-existing-match")
        val userId = UserId("test-user")

        coEvery { repository.findById(matchId) } returns null

        // When
        assertFailsWith<MatchNotFoundException> { matchService.getMatch(matchId, userId) }
    }

    @Test
    fun `getMatch should throw exception when user not verified`(): Unit = runBlocking {
        // Given
        val userId = UserId("test-user")
        val match = TestMatchBuilder().toProjection()

        coEvery { repository.findById(match.id) } returns match
        coEvery { groupApi.isActiveMember(match.groupId, userId) } returns false

        // When
        assertFailsWith<UserNotAuthorizedException> { matchService.getMatch(match.id, userId) }
    }

    @Test
    fun `getMatchesByGroup should get matches`(): Unit = runBlocking {
        // Given
        val match = TestMatchBuilder().toProjection()
        val userId = UserId("test-user")

        coEvery { repository.findAllByGroupId(match.groupId, MatchFilter()) } returns listOf(match)
        coEvery { groupApi.isActiveMember(match.groupId, userId) } returns true

        // When
        matchService.getMatchesByGroup(match.groupId, userId, MatchFilter()).let { result ->
            // Then
            assertThat(result).isNotNull().hasSize(1)
        }
    }

    @Test
    fun `getMatchesByGroup should throw exception when user not verified`(): Unit = runBlocking {
        // Given
        val userId = UserId("test-user")
        val match = TestMatchBuilder().toProjection()

        coEvery { groupApi.isActiveMember(match.groupId, userId) } returns false

        // When
        assertFailsWith<UserNotAuthorizedException> { matchService.getMatchesByGroup(match.groupId, userId, MatchFilter()) }
    }

    @Test
    fun `getPlayerMatches should get matches`(): Unit = runBlocking {
        // Given
        val playerId = UserId("test-player")
        val match = TestMatchBuilder().toProjection()

        coEvery { userApi.getGroups(playerId) } returns listOf(match.groupId)
        coEvery { repository.findAllByGroupId(match.groupId, MatchFilter()) } returns listOf(match)

        // When
        matchService.getPlayerMatches(playerId).let { result ->
            // Then
            assertThat(result).isNotNull().hasSize(1)
        }
    }

    @Test
    fun `whenEvent should handle match planned event`(): Unit = runBlocking {
        // Given
        val match = TestMatchBuilder().toProjection()
        val event = MatchPlannedEvent(
            aggregateId = match.id.value,
            groupId = match.groupId,
            start = match.start,
            playground = match.playground,
            maxPlayer = match.maxPlayer,
            minPlayer = match.minPlayer
        )

        coEvery { repository.save(any()) } returns Unit

        // When
        matchService.whenEvent(event)

        // Then
        coEvery { repository.save(match) }
    }

    @Test
    fun `whenEvent should handle player added to cadre event`(): Unit = runBlocking {
        // Given
        val match = TestMatchBuilder().toProjection()
        val event = PlayerAddedToCadreEvent(
            aggregateId = match.id.value,
            userId = match.deregisteredPlayers.first(),
            status = "ADDED",
        )

        coEvery { repository.findById(match.id) } returns match
        coEvery { repository.save(match.copy(
            cadrePlayers = match.cadrePlayers + event.userId,
            deregisteredPlayers = match.deregisteredPlayers - event.userId,
        )) } returns Unit

        // When
        matchService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle player deregistered event`(): Unit = runBlocking {
        // Given
        val match = TestMatchBuilder().toProjection()
        val event = PlayerDeregisteredEvent(
            aggregateId = match.id.value,
            userId = match.cadrePlayers.first(),
            status = "ADDED"
        )

        coEvery { repository.findById(match.id) } returns match
        coEvery { repository.save(match.copy(
            cadrePlayers = match.cadrePlayers - event.userId,
            deregisteredPlayers = match.deregisteredPlayers + event.userId
        )) } returns Unit

        // When
        matchService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle player placed on waiting bench event`(): Unit = runBlocking {
        // Given
        val match = TestMatchBuilder().toProjection()
        val event = PlayerPlacedOnWaitingBenchEvent(
            aggregateId = match.id.value,
            userId = match.cadrePlayers.first(),
            status = "ADDED"
        )

        coEvery { repository.findById(match.id) } returns match
        coEvery { repository.save(match.copy(
            cadrePlayers = match.cadrePlayers - event.userId,
            waitingBenchPlayers = match.waitingBenchPlayers + event.userId
        )) } returns Unit

        // When
        matchService.whenEvent(event)

    }

    @Test
    fun `when event should handle match canceled event`(): Unit = runBlocking {
        // Given
        val match = TestMatchBuilder().toProjection()
        val event = MatchCanceledEvent(
            aggregateId = match.id.value,
            groupId = match.groupId
        )

        coEvery { repository.findById(match.id) } returns match
        coEvery { repository.save(match.copy(isCanceled = true)) } returns Unit

        // When
        matchService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle playground changed event`(): Unit = runBlocking {
        // Given
        val match = TestMatchBuilder().toProjection()
        val newPlayground = "New Playground"
        val event = PlaygroundChangedEvent(
            aggregateId = match.id.value,
            groupId = match.groupId,
            newPlayground = newPlayground
        )

        coEvery { repository.findById(match.id) } returns match
        coEvery { repository.save(match.copy(playground = newPlayground)) } returns Unit

        // When
        matchService.whenEvent(event)
    }

    @Test
    fun `whenEvent should handle match result entered event`(): Unit = runBlocking {
        // Given
        val match = TestMatchBuilder().toProjection()
        val event = MatchResultEnteredEvent(
            aggregateId = match.id.value,
            groupId = match.groupId,
            players = match.result,
            start = LocalDateTime.now()
        )

        coEvery { repository.findById(match.id) } returns match
        coEvery { repository.save(match.copy(result = event.players)) } returns Unit

        // When
        matchService.whenEvent(event)
    }

}