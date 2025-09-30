package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
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
            playerCount = PlayerCount(MinPlayer(8), MaxPlayer(16)),
            requesterId = UserId("requesterId")
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
    fun `cancelMatch should cancel match`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().plusDays(1)

        // When
        matchAggregate.cancelMatch()

        // Then
        assertThat(matchAggregate.isCanceled).isEqualTo(true)
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
    fun `enterResult should throw exception if match is not started`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().plusDays(1)

        val participatingPlayers = emptyList<ParticipatingPlayer>()

        assertThatThrownBy {
            // When
            matchAggregate.enterResult(participatingPlayers)

            // Then
        }.isInstanceOf(MatchStartTimeException::class.java)
    }

    @Test
    fun `enterResult should throw exception if match is canceled`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().minusDays(1)
        matchAggregate.isCanceled = true

        val participatingPlayers = emptyList<ParticipatingPlayer>()

        assertThatThrownBy {
            // When
            matchAggregate.enterResult( participatingPlayers)

            // Then
        }.isInstanceOf(MatchCanceledException::class.java)
    }

    @Test
    fun `addRegistration should throw exception if already started`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().minusDays(1)

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
        matchAggregate.start = LocalDateTime.now().plusDays(1)
        matchAggregate.playerCount = PlayerCount(MinPlayer(4), MaxPlayer(6))
        testData.cadre.forEach { matchAggregate.cadre.add(it) }
        val newPlayerId = UserId("new player")

        // When
        matchAggregate.addRegistration(newPlayerId, testData.registrationStatusType)

        // Then
        assertThat(matchAggregate.cadre.size).isEqualTo(testData.cadre.size + testData.addedPlayers)
        if (testData.addedPlayers == 1 || testData.waitingPlayers == 1) {
            assertThat(matchAggregate.changes.size).isEqualTo(1)
            assertThat(matchAggregate.changes.first()).isInstanceOf(testData.expectedEventType)
        }
        if (testData.addedPlayers == 1) {
            matchAggregate.cadre.filterIsInstance<RegisteredPlayer.MainPlayer>().find { it.userId == newPlayerId }.let { player ->
                assertThat(player).isNotNull()
                assertThat(player?.status).isEqualTo(testData.exceptedStatus)
            }
        }
        if (testData.waitingPlayers == 1) {
            matchAggregate.waitingBench.filterIsInstance<RegisteredPlayer.MainPlayer>().find { it.userId == newPlayerId }.let { player ->
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
        matchAggregate.start = LocalDateTime.now().plusDays(1)
        matchAggregate.playerCount = PlayerCount(MinPlayer(4), MaxPlayer(6))
        testData.cadre.forEach { matchAggregate.cadre.add(it) }
        when (testData.oldRegistrationStatus) {
            RegistrationStatus.Added -> matchAggregate.cadre.add(RegisteredPlayer.MainPlayer(newPlayerId, 0,LocalDateTime.now(), testData.oldRegistrationStatus))
            RegistrationStatus.Cancelled -> matchAggregate.waitingBench.add(RegisteredPlayer.MainPlayer(newPlayerId, 0,LocalDateTime.now(), testData.oldRegistrationStatus))
            RegistrationStatus.Deregistered -> matchAggregate.deregistered.add(RegisteredPlayer.MainPlayer(newPlayerId, 0,LocalDateTime.now(), testData.oldRegistrationStatus))
            RegistrationStatus.Registered -> matchAggregate.cadre.add(RegisteredPlayer.MainPlayer(newPlayerId, 0,LocalDateTime.now(), testData.oldRegistrationStatus))
        }

        // When
        matchAggregate.addRegistration(newPlayerId, testData.newRegistrationStatusType)

        // Then
        assertThat(matchAggregate.cadre.size).isEqualTo(testData.cadre.size + 1 - testData.deregisteredPlayers - testData.placedOnWaitingBench - testData.canceledPlayer)
        when {
            testData.deregisteredPlayers == 1 -> {
                matchAggregate.deregistered.filterIsInstance<RegisteredPlayer.MainPlayer>().find { it.userId == newPlayerId }.let { player ->
                    assertThat(player).isNotNull()
                    assertThat(player?.status).isEqualTo(testData.exceptedStatus)
                }
            }

            testData.placedOnWaitingBench == 1 -> {
                matchAggregate.waitingBench.filterIsInstance<RegisteredPlayer.MainPlayer>().find { it.userId == newPlayerId }.let { player ->
                    assertThat(player).isNotNull()
                    assertThat(player?.status).isEqualTo(testData.exceptedStatus)
                }
            }

            testData.canceledPlayer == 1 -> {
                matchAggregate.waitingBench.filterIsInstance<RegisteredPlayer.MainPlayer>().find { it.userId == newPlayerId }.let { player ->
                    assertThat(player).isNotNull()
                    assertThat(player?.status).isEqualTo(testData.exceptedStatus)
                }
            }

            else -> {
                matchAggregate.cadre.filterIsInstance<RegisteredPlayer.MainPlayer>().find { it.userId == newPlayerId }.let { player ->
                    assertThat(player).isNotNull()
                    assertThat(player?.status).isEqualTo(testData.exceptedStatus)
                }
            }
        }
        if (testData.expectedEventType != null) {
            assertThat(matchAggregate.changes.size).isEqualTo(1)
            assertThat(matchAggregate.changes.first()).isInstanceOf(testData.expectedEventType)
        } else {
            assertThat(matchAggregate.changes.size).isEqualTo(0)
        }
    }

    @ParameterizedTest
    @MethodSource("addedWaitingPlayers")
    fun `addRegistration should add waiting player when deregistered or canceled`(testData: AddWaitingPlayerTestData) {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.playerCount = PlayerCount(MinPlayer(4), MaxPlayer(6))
        matchAggregate.start = LocalDateTime.now().plusDays(1)

        testData.cadre.forEach { matchAggregate.cadre.add(it) }
        testData.waitingPlayers.forEach { matchAggregate.waitingBench.add(it) }

        // When
        matchAggregate.addRegistration(testData.userId, testData.registrationStatusType)

        // Then
        assertThat(matchAggregate.waitingBench.size).isEqualTo(testData.expectedWaitingPlayers)
        assertThat(matchAggregate.cadre.size).isEqualTo(testData.expectedCadrePlayers)

        for (player in testData.expectedUserIds) {
            matchAggregate.cadre.filterIsInstance<RegisteredPlayer.MainPlayer>().find { it.userId == player }.let { player ->
                assertThat(player).isNotNull()
            }
        }

        if (testData.expectedEventType.isNotEmpty()) {
            assertThat(matchAggregate.changes.size).isEqualTo(testData.expectedEventType.size)
            for ((index, event) in testData.expectedEventType.withIndex()) {
                assertThat(matchAggregate.changes[index]).isInstanceOf(event)
            }
        } else {
            assertThat(matchAggregate.changes.size).isEqualTo(0)
        }
    }

    @ParameterizedTest
    @MethodSource("registeredGuestPlayers")
    fun `addRegistration with guest should add registration with guests`(testData: RegisteredPlayerWithGuestTestData) {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.playerCount = PlayerCount(MinPlayer(4), MaxPlayer(6))
        matchAggregate.start = LocalDateTime.now().plusDays(1)

        testData.cadre.forEach { matchAggregate.cadre.add(it) }

        // When
        matchAggregate.addRegistration(testData.userId, testData.registrationStatusType, testData.guests)

        // Then
        assertThat(matchAggregate.cadre.filterIsInstance<RegisteredPlayer.MainPlayer>()).hasSize(testData.expectedCadreMainPlayers)
        assertThat(matchAggregate.cadre.filterIsInstance<RegisteredPlayer.GuestPlayer>()).hasSize(testData.expectedCadreGuestPlayers)
        assertThat(matchAggregate.waitingBench.filterIsInstance<RegisteredPlayer.MainPlayer>()).hasSize(testData.expectedWaitingBenchMainPlayers)
        assertThat(matchAggregate.waitingBench.filterIsInstance<RegisteredPlayer.GuestPlayer>()).hasSize(testData.expectedWaitingBenchGuestPlayers)
    }

    companion object {
        data class AddWaitingPlayerTestData(
            val userId: UserId,
            val registrationStatusType: RegistrationStatusType,
            val cadre: List<RegisteredPlayer>,
            val waitingPlayers: List<RegisteredPlayer>,
            val expectedWaitingPlayers: Int = 0,
            val expectedCadrePlayers: Int = 0,
            val expectedEventType: List<Class<out BaseEvent>> = emptyList(),
            val expectedUserIds: List<UserId> = emptyList()
        )

        data class UnregisteredPlayerTestData(
            val registrationStatusType: RegistrationStatusType,
            val cadre: List<RegisteredPlayer>,
            val addedPlayers: Int = 0,
            val waitingPlayers: Int = 0,
            val exceptedStatus: RegistrationStatus? = null,
            val expectedEventType: Class<out BaseEvent>? = null
        )

        data class RegisteredPlayerTestData(
            val newRegistrationStatusType: RegistrationStatusType,
            val oldRegistrationStatus: RegistrationStatus,
            val cadre: List<RegisteredPlayer>,
            val deregisteredPlayers: Int = 0,
            val placedOnWaitingBench: Int = 0,
            val canceledPlayer: Int = 0,
            val exceptedStatus: RegistrationStatus,
            val expectedEventType: Class<out BaseEvent>? = null
        )

        data class RegisteredPlayerWithGuestTestData(
            val userId: UserId,
            val registrationStatusType: RegistrationStatusType,
            val guests: Int,
            val cadre: List<RegisteredPlayer>,
            val expectedCadreMainPlayers: Int = 0,
            val expectedCadreGuestPlayers: Int = 0,
            val expectedWaitingBenchMainPlayers: Int = 0,
            val expectedWaitingBenchGuestPlayers: Int = 0
        )

        val fullCadreWithGuests = listOf(
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 1"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 2"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 3"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.GuestPlayer(
                guestId = "guest 1",
                guestOf = UserId("player 3"),
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.GuestPlayer(
                guestId = "guest 2",
                guestOf = UserId("player 3"),
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 6"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Added
            )
        )

        val fullCadre = listOf(
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 1"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 2"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 3"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 4"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 5"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 6"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Added
            )
        )
        val nonFullCadre = listOf(
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 1"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 2"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 3"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 4"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
            RegisteredPlayer.MainPlayer(
                userId = UserId("player 5"),
                guests = 0,
                registeredAt = LocalDateTime.now(),
                registrationStatus = RegistrationStatus.Registered
            ),
        )

        @JvmStatic
        fun registeredGuestPlayers() = listOf(
            RegisteredPlayerWithGuestTestData(
                userId = UserId("new player"),
                registrationStatusType = RegistrationStatusType.REGISTERED,
                guests = 2,
                cadre = nonFullCadre,
                expectedCadreMainPlayers = 6,
                expectedWaitingBenchGuestPlayers = 2
            ),
            RegisteredPlayerWithGuestTestData(
                userId = UserId("new player"),
                registrationStatusType = RegistrationStatusType.REGISTERED,
                guests = 2,
                cadre = emptyList(),
                expectedCadreMainPlayers = 1,
                expectedCadreGuestPlayers = 2
            ),
            RegisteredPlayerWithGuestTestData(
                userId = UserId("new player"),
                registrationStatusType = RegistrationStatusType.REGISTERED,
                guests = 2,
                cadre = fullCadreWithGuests,
                expectedCadreMainPlayers = 5,
                expectedCadreGuestPlayers = 1,
                expectedWaitingBenchGuestPlayers = 3
            ),
            RegisteredPlayerWithGuestTestData(
                userId = UserId("new player"),
                registrationStatusType = RegistrationStatusType.REGISTERED,
                guests = 2,
                cadre = fullCadre,
                expectedCadreMainPlayers = 6,
                expectedWaitingBenchMainPlayers = 1,
                expectedWaitingBenchGuestPlayers = 2
            ),
            RegisteredPlayerWithGuestTestData(
                userId = UserId("new player"),
                registrationStatusType = RegistrationStatusType.REGISTERED,
                guests = 2,
                cadre = fullCadre.subList(0, 4),
                expectedCadreMainPlayers = 5,
                expectedCadreGuestPlayers = 1,
                expectedWaitingBenchGuestPlayers = 1
            ),
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
                deregisteredPlayers = 1,
                exceptedStatus = RegistrationStatus.Deregistered,
                expectedEventType = PlayerDeregisteredEvent::class.java
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.CANCELLED,
                oldRegistrationStatus = RegistrationStatus.Registered,
                cadre = fullCadre,
                placedOnWaitingBench = 1,
                exceptedStatus = RegistrationStatus.Cancelled,
                expectedEventType = PlayerPlacedOnWaitingBenchEvent::class.java
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.REGISTERED,
                oldRegistrationStatus = RegistrationStatus.Deregistered,
                cadre = fullCadre,
                placedOnWaitingBench = 1,
                exceptedStatus = RegistrationStatus.Registered,
                expectedEventType = PlayerPlacedOnWaitingBenchEvent::class.java
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
                deregisteredPlayers = 1,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Deregistered,
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.ADDED,
                oldRegistrationStatus = RegistrationStatus.Deregistered,
                deregisteredPlayers = 1,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Deregistered,
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.CANCELLED,
                oldRegistrationStatus = RegistrationStatus.Deregistered,
                deregisteredPlayers = 1,
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
                deregisteredPlayers = 1,
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
                canceledPlayer = 1,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Cancelled,
                expectedEventType = PlayerPlacedOnWaitingBenchEvent::class.java
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.REGISTERED,
                oldRegistrationStatus = RegistrationStatus.Cancelled,
                canceledPlayer = 1,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Cancelled,
            ),
            RegisteredPlayerTestData(
                newRegistrationStatusType = RegistrationStatusType.DEREGISTERED,
                oldRegistrationStatus = RegistrationStatus.Cancelled,
                canceledPlayer = 1,
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
                canceledPlayer = 1,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Cancelled,
            ),
        ).stream()

        @JvmStatic
        fun addedWaitingPlayers() = listOf(
            AddWaitingPlayerTestData(
                userId = UserId("player 1"),
                registrationStatusType = RegistrationStatusType.DEREGISTERED,
                cadre = fullCadre,
                waitingPlayers = listOf(RegisteredPlayer.MainPlayer(
                    userId = UserId("cancelledPlayer"),
                    guests = 0,
                    registeredAt = LocalDateTime.now().minusHours(2),
                    registrationStatus = RegistrationStatus.Cancelled
                ),RegisteredPlayer.MainPlayer(
                    userId = UserId("notSoLongWaitingPlayer"),
                    guests = 0,
                    registeredAt = LocalDateTime.now(),
                    registrationStatus = RegistrationStatus.Registered
                ),RegisteredPlayer.MainPlayer(
                    userId = UserId("waitingPlayer"),
                    guests = 0,
                    registeredAt = LocalDateTime.now().minusHours(1),
                    registrationStatus = RegistrationStatus.Registered
                )),
                expectedWaitingPlayers = 2,
                expectedCadrePlayers = fullCadre.size,
                expectedEventType = listOf(PlayerDeregisteredEvent::class.java, PlayerAddedToCadreEvent::class.java),
                expectedUserIds = listOf(UserId("waitingPlayer"))
            ),
            AddWaitingPlayerTestData(
                userId = UserId("player 1"),
                registrationStatusType = RegistrationStatusType.CANCELLED,
                cadre = fullCadre,
                waitingPlayers = listOf(RegisteredPlayer.MainPlayer(
                    userId = UserId("cancelledPlayer"),
                    guests = 0,
                    registeredAt = LocalDateTime.now().minusHours(2),
                    registrationStatus = RegistrationStatus.Cancelled
                ),RegisteredPlayer.MainPlayer(
                    userId = UserId("notSoLongWaitingPlayer"),
                    guests = 0,
                    registeredAt = LocalDateTime.now(),
                    registrationStatus = RegistrationStatus.Registered
                ),RegisteredPlayer.MainPlayer(
                    userId = UserId("waitingPlayer"),
                    guests = 0,
                    registeredAt = LocalDateTime.now().minusHours(1),
                    registrationStatus = RegistrationStatus.Registered
                )),
                expectedWaitingPlayers = 3,
                expectedCadrePlayers = fullCadre.size,
                expectedEventType = listOf(PlayerPlacedOnWaitingBenchEvent::class.java, PlayerAddedToCadreEvent::class.java),
                expectedUserIds = listOf(UserId("waitingPlayer"))
            ),
        ).stream()

        @JvmStatic
        fun unregisteredPlayers() = listOf(
            UnregisteredPlayerTestData(
                registrationStatusType = RegistrationStatusType.REGISTERED,
                cadre = fullCadre,
                waitingPlayers = 1,
                exceptedStatus = RegistrationStatus.Registered,
                expectedEventType = PlayerPlacedOnWaitingBenchEvent::class.java
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
                cadre = nonFullCadre,
                exceptedStatus = RegistrationStatus.Registered,
                expectedEventType = PlayerDeregisteredEvent::class.java
            ),
            UnregisteredPlayerTestData(
                registrationStatusType = RegistrationStatusType.DEREGISTERED,
                cadre = fullCadre,
                exceptedStatus = RegistrationStatus.Deregistered,
                expectedEventType = PlayerDeregisteredEvent::class.java
            ),
            UnregisteredPlayerTestData(
                registrationStatusType = RegistrationStatusType.CANCELLED,
                cadre = fullCadre,
            ),
            UnregisteredPlayerTestData(
                registrationStatusType = RegistrationStatusType.ADDED,
                cadre = fullCadre,
            ),
        ).stream()
    }

}