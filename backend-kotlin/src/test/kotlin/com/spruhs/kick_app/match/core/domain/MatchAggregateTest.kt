package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnSubstituteBenchEvent
import com.spruhs.kick_app.match.core.application.PlanMatchCommand
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime


class MatchAggregateTest {

    @Test
    fun `planMatch should plan a match`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")

        val command = PlanMatchCommand(
            groupId = GroupId("groupId"),
            start = LocalDateTime.now(),
            playground = Playground("stadium"),
            playerCount = PlayerCount(MinPlayer(8), MaxPlayer(16))
        )

        // When
        matchAggregate.planMatch(command)

        // Then
        assertThat(matchAggregate.groupId).isEqualTo(command.groupId)
        assertThat(matchAggregate.start).isEqualTo(command.start)
        assertThat(matchAggregate.playground).isEqualTo(command.playground)
        assertThat(matchAggregate.playerCount).isEqualTo(command.playerCount)
    }

    @Test
    fun `changePlayground should change playground`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.playground = Playground("stadium")

        // When
        matchAggregate.changePlayground(Playground("arena"))

        // Then
        assertThat(matchAggregate.playground).isEqualTo(Playground("arena"))
    }

    @Test
    fun `startMatch should start match`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now()

        // When
        matchAggregate.startMatch()

        // Then
        assertThat(matchAggregate.status).isEqualTo(MatchStatus.ENTER_RESULT)
    }

    @Test
    fun `startMatch should throw exception if start is in the future`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().plusDays(1)


        assertThatThrownBy {
            // When
            matchAggregate.startMatch()

            // Then
        }.isInstanceOf(MatchStartTimeException::class.java)
    }

    @Test
    fun `cancelMatch should cancel match`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().plusDays(1)

        // When
        matchAggregate.cancelMatch()

        // Then
        assertThat(matchAggregate.status).isEqualTo(MatchStatus.CANCELLED)
    }

    @Test
    fun `cancelMatch should throw exception if start is in the past`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now()


        assertThatThrownBy {
            // When
            matchAggregate.cancelMatch()

            // Then
        }.isInstanceOf(MatchStartTimeException::class.java)
    }

    @Test
    fun `enterResult should enter result`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().minusDays(1)

        val result = Result.WINNER_TEAM_A
        val participatingPlayers =
            listOf(ParticipatingPlayer(UserId("player 1"), Team.A), ParticipatingPlayer(UserId("player 2"), Team.B))

        // When
        matchAggregate.enterResult(result, participatingPlayers)

        // Then
        assertThat(matchAggregate.result).isEqualTo(result)
        assertThat(matchAggregate.participatingPlayers).isEqualTo(participatingPlayers)
    }

    @Test
    fun `enterResult should throw exception if match is not started`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().plusDays(1)

        val result = Result.WINNER_TEAM_A
        val participatingPlayers = emptyList<ParticipatingPlayer>()

        assertThatThrownBy {
            // When
            matchAggregate.enterResult(result, participatingPlayers)

            // Then
        }.isInstanceOf(MatchStartTimeException::class.java)
    }

    @Test
    fun `enterResult should throw exception if match is canceled`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().minusDays(1)
        matchAggregate.status = MatchStatus.CANCELLED

        val result = Result.WINNER_TEAM_A
        val participatingPlayers = emptyList<ParticipatingPlayer>()

        assertThatThrownBy {
            // When
            matchAggregate.enterResult(result, participatingPlayers)

            // Then
        }.isInstanceOf(MatchCanceledException::class.java)
    }

    @Test
    fun `enterResult should throw exception if player entered multiple times`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().minusDays(1)

        val result = Result.WINNER_TEAM_A
        val participatingPlayers = listOf(
            ParticipatingPlayer(UserId("player 1"), Team.A),
            ParticipatingPlayer(UserId("player 1"), Team.B)
        )

        assertThatThrownBy {
            // When
            matchAggregate.enterResult(result, participatingPlayers)

            // Then
        }.isInstanceOf(PlayerResultEnteredMultipleTimesException::class.java)
    }

    @Test
    fun `addRegistration should throw exception if already started`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().plusDays(1)

        assertThatThrownBy {
            // When
            matchAggregate.addRegistration(UserId("player 1"), RegistrationStatusType.REGISTERED)

            // Then
        }.isInstanceOf(MatchStartTimeException::class.java)
    }

    @ParameterizedTest
    @MethodSource("unregisteredPlayers")
    fun `addRegistration should add unregistered player`(testData: UnregisteredPlayerTestData) {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().minusDays(1)
        matchAggregate.playerCount = PlayerCount(MinPlayer(4), MaxPlayer(6))
        matchAggregate.registeredPlayers = testData.cadre
        val newPlayerId = UserId("new player")

        // When
        matchAggregate.addRegistration(newPlayerId, testData.registrationStatusType)

        // Then
        assertThat(matchAggregate.registeredPlayers.size).isEqualTo(testData.cadre.size + testData.addedPlayers)
        if (testData.addedPlayers == 1) {
            assertThat(matchAggregate.changes.size).isEqualTo(1)
            assertThat(matchAggregate.changes.first()).isInstanceOf(testData.expectedEventType)
            matchAggregate.registeredPlayers.find { it.userId == newPlayerId }.let { player ->
                assertThat(player).isNotNull()
                assertThat(player?.status).isEqualTo(testData.exceptedStatus)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("registeredPlayers")
    fun `addRegistration should add registered player`(testData: RegisteredPlayerTestData) {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        val newPlayerId = UserId("new player")
        matchAggregate.start = LocalDateTime.now().minusDays(1)
        matchAggregate.playerCount = PlayerCount(MinPlayer(4), MaxPlayer(6))
        matchAggregate.registeredPlayers = testData.cadre + RegisteredPlayer(newPlayerId, LocalDateTime.now(), testData.oldRegistrationStatus)

        // When
        matchAggregate.addRegistration(newPlayerId, testData.newRegistrationStatusType)

        // Then
        assertThat(matchAggregate.registeredPlayers.size).isEqualTo(testData.cadre.size + 1)
        matchAggregate.registeredPlayers.find { it.userId == newPlayerId }.let { player ->
            assertThat(player).isNotNull()
            assertThat(player?.status).isEqualTo(testData.exceptedStatus)
        }
        if (testData.expectedEventType != null) {
            assertThat(matchAggregate.changes.size).isEqualTo(1)
            assertThat(matchAggregate.changes.first()).isInstanceOf(testData.expectedEventType)
        } else {
            assertThat(matchAggregate.changes.size).isEqualTo(0)
        }
    }

    companion object {

        data class UnregisteredPlayerTestData(
            val registrationStatusType: RegistrationStatusType,
            val cadre: List<RegisteredPlayer>,
            val addedPlayers: Int,
            val exceptedStatus: RegistrationStatus? = null,
            val expectedEventType: Class<out BaseEvent>? = null
        )

        data class RegisteredPlayerTestData(
            val newRegistrationStatusType: RegistrationStatusType,
            val oldRegistrationStatus: RegistrationStatus,
            val cadre: List<RegisteredPlayer>,
            val exceptedStatus: RegistrationStatus,
            val expectedEventType: Class<out BaseEvent>? = null
        )

        val fullCadre = listOf(
            RegisteredPlayer(
                userId = UserId("player 1"),
                registrationTime = LocalDateTime.now(),
                status = RegistrationStatus.Registered
            ),
            RegisteredPlayer(
                userId = UserId("player 2"),
                registrationTime = LocalDateTime.now(),
                status = RegistrationStatus.Registered
            ),
            RegisteredPlayer(
                userId = UserId("player 3"),
                registrationTime = LocalDateTime.now(),
                status = RegistrationStatus.Registered
            ),
            RegisteredPlayer(
                userId = UserId("player 4"),
                registrationTime = LocalDateTime.now(),
                status = RegistrationStatus.Registered
            ),
            RegisteredPlayer(
                userId = UserId("player 5"),
                registrationTime = LocalDateTime.now(),
                status = RegistrationStatus.Registered
            ),
            RegisteredPlayer(
                userId = UserId("player 6"),
                registrationTime = LocalDateTime.now(),
                status = RegistrationStatus.Added
            )
        )
        val nonFullCadre = listOf(
            RegisteredPlayer(
                userId = UserId("player 1"),
                registrationTime = LocalDateTime.now(),
                status = RegistrationStatus.Registered
            ),
            RegisteredPlayer(
                userId = UserId("player 2"),
                registrationTime = LocalDateTime.now(),
                status = RegistrationStatus.Registered
            ),
            RegisteredPlayer(
                userId = UserId("player 3"),
                registrationTime = LocalDateTime.now(),
                status = RegistrationStatus.Registered
            ),
            RegisteredPlayer(
                userId = UserId("player 4"),
                registrationTime = LocalDateTime.now(),
                status = RegistrationStatus.Registered
            ),
            RegisteredPlayer(
                userId = UserId("player 5"),
                registrationTime = LocalDateTime.now(),
                status = RegistrationStatus.Registered
            ),
            RegisteredPlayer(
                userId = UserId("player 6"),
                registrationTime = LocalDateTime.now(),
                status = RegistrationStatus.Cancelled
            ),
            RegisteredPlayer(
                userId = UserId("player 7"),
                registrationTime = LocalDateTime.now(),
                status = RegistrationStatus.Deregistered
            )
        )

        @JvmStatic
        fun registeredPlayers() = listOf(
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.REGISTERED,
                oldRegistrationStatus = RegistrationStatus.Registered,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Registered,
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.ADDED,
                oldRegistrationStatus = RegistrationStatus.Registered,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Registered,
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.DEREGISTERED,
                oldRegistrationStatus = RegistrationStatus.Registered,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Deregistered,
                expectedEventType = PlayerDeregisteredEvent::class.java
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.CANCELLED,
                oldRegistrationStatus = RegistrationStatus.Registered,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Cancelled,
                expectedEventType = PlayerPlacedOnSubstituteBenchEvent::class.java
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.REGISTERED,
                oldRegistrationStatus = RegistrationStatus.Deregistered,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Registered,
                expectedEventType = PlayerPlacedOnSubstituteBenchEvent::class.java
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.REGISTERED,
                oldRegistrationStatus = RegistrationStatus.Deregistered,
                cadre = nonFullCadre,
                exceptedStatus = RegistrationStatus.Registered,
                expectedEventType = PlayerAddedToCadreEvent::class.java
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.DEREGISTERED,
                oldRegistrationStatus = RegistrationStatus.Deregistered,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Deregistered,
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.ADDED,
                oldRegistrationStatus = RegistrationStatus.Deregistered,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Deregistered,
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.CANCELLED,
                oldRegistrationStatus = RegistrationStatus.Deregistered,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Deregistered,
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.REGISTERED,
                oldRegistrationStatus = RegistrationStatus.Added,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Added,
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.DEREGISTERED,
                oldRegistrationStatus = RegistrationStatus.Added,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Deregistered,
                expectedEventType = PlayerDeregisteredEvent::class.java
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.ADDED,
                oldRegistrationStatus = RegistrationStatus.Added,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Added,
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.CANCELLED,
                oldRegistrationStatus = RegistrationStatus.Added,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Cancelled,
                expectedEventType = PlayerPlacedOnSubstituteBenchEvent::class.java
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.REGISTERED,
                oldRegistrationStatus = RegistrationStatus.Cancelled,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Cancelled,
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.DEREGISTERED,
                oldRegistrationStatus = RegistrationStatus.Cancelled,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Cancelled,
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.ADDED,
                oldRegistrationStatus = RegistrationStatus.Cancelled,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Added,
                expectedEventType = PlayerAddedToCadreEvent::class.java
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.CANCELLED,
                oldRegistrationStatus = RegistrationStatus.Cancelled,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Cancelled,
            ),
        ).stream()

        @JvmStatic
        fun unregisteredPlayers() = listOf(
            UnregisteredPlayerTestData(
                registrationStatusType = RegistrationStatusType.REGISTERED,
                cadre = fullCadre,
                addedPlayers = 1,
                exceptedStatus = RegistrationStatus.Registered,
                expectedEventType = PlayerPlacedOnSubstituteBenchEvent::class.java
            ),
            UnregisteredPlayerTestData(
                registrationStatusType = RegistrationStatusType.REGISTERED,
                cadre = nonFullCadre,
                addedPlayers = 1,
                exceptedStatus = RegistrationStatus.Registered,
                expectedEventType = PlayerAddedToCadreEvent::class.java
            ),
            UnregisteredPlayerTestData(
                registrationStatusType = RegistrationStatusType.REGISTERED,
                cadre = nonFullCadre,
                addedPlayers = 1,
                exceptedStatus = RegistrationStatus.Registered,
                expectedEventType = PlayerAddedToCadreEvent::class.java
            ),
            UnregisteredPlayerTestData(
                registrationStatusType = RegistrationStatusType.DEREGISTERED,
                cadre = fullCadre,
                addedPlayers = 1,
                exceptedStatus = RegistrationStatus.Deregistered,
                expectedEventType = PlayerDeregisteredEvent::class.java
            ),
            UnregisteredPlayerTestData(
                registrationStatusType = RegistrationStatusType.CANCELLED,
                cadre = fullCadre,
                addedPlayers = 0,
            ),
            UnregisteredPlayerTestData(
                registrationStatusType = RegistrationStatusType.ADDED,
                cadre = fullCadre,
                addedPlayers = 0,
            ),
        ).stream()
    }

}