package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.match.api.MatchNumber
import com.spruhs.kick_app.match.api.MatchResultUpdatedEvent
import com.spruhs.kick_app.match.api.MatchTeam
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.api.PlayerOverviewEntry
import com.spruhs.kick_app.match.api.PlayerResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PlayerOverviewTest {

    private val groupId = GroupId("group1")

    @Test
    fun `enterResult should give participating player +5 attendance points`() {
        val player = UserId("player1")
        val overview = PlayerOverview(groupId, mutableListOf(PlayerOverviewEntry(player, attendancePoints = 10)))
        val match = buildMatch(cadre = listOf(player), result = listOf(player))

        overview.enterResult(match)

        assertThat(overview.entries.first { it.userId == player }.attendancePoints).isEqualTo(15)
    }

    @Test
    fun `enterResult should reset lastWaitingBenchMatchNumber for participating player`() {
        val player = UserId("player1")
        val overview =
            PlayerOverview(
                groupId,
                mutableListOf(PlayerOverviewEntry(player, attendancePoints = 10, lastWaitingBenchMatchNumber = MatchNumber(3))),
            )
        val match = buildMatch(cadre = listOf(player), result = listOf(player))

        overview.enterResult(match)

        assertThat(overview.entries.first { it.userId == player }.lastWaitingBenchMatchNumber).isNull()
    }

    @Test
    fun `enterResult should create new entry with 5 points for new participating player`() {
        val player = UserId("newPlayer")
        val overview = PlayerOverview(groupId)
        val match = buildMatch(cadre = listOf(player), result = listOf(player))

        overview.enterResult(match)

        val entry = overview.entries.firstOrNull { it.userId == player }
        assertThat(entry).isNotNull()
        assertThat(entry!!.attendancePoints).isEqualTo(5)
    }

    @Test
    fun `enterResult should subtract 3 points from cadre player who did not participate`() {
        val player = UserId("absentCadrePlayer")
        val overview = PlayerOverview(groupId, mutableListOf(PlayerOverviewEntry(player, attendancePoints = 10)))
        val match = buildMatch(cadre = listOf(player), result = emptyList())

        overview.enterResult(match)

        assertThat(overview.entries.first { it.userId == player }.attendancePoints).isEqualTo(7)
    }

    @Test
    fun `enterResult should not let cadre player points go below 0`() {
        val player = UserId("absentCadrePlayer")
        val overview = PlayerOverview(groupId, mutableListOf(PlayerOverviewEntry(player, attendancePoints = 2)))
        val match = buildMatch(cadre = listOf(player), result = emptyList())

        overview.enterResult(match)

        assertThat(overview.entries.first { it.userId == player }.attendancePoints).isEqualTo(0)
    }

    @Test
    fun `enterResult should give waiting bench player +3 points when not participating`() {
        val player = UserId("benchPlayer")
        val matchNumber = MatchNumber(7)
        val overview = PlayerOverview(groupId, mutableListOf(PlayerOverviewEntry(player, attendancePoints = 4)))
        val match = buildMatch(waitingBench = listOf(player), result = emptyList(), matchNumber = matchNumber)

        overview.enterResult(match)

        val entry = overview.entries.first { it.userId == player }
        assertThat(entry.attendancePoints).isEqualTo(7)
        assertThat(entry.lastWaitingBenchMatchNumber).isEqualTo(matchNumber)
    }

    @Test
    fun `enterResult should subtract 1 point from player not involved in match`() {
        val uninvolvedPlayer = UserId("uninvolved")
        val overview = PlayerOverview(groupId, mutableListOf(PlayerOverviewEntry(uninvolvedPlayer, attendancePoints = 6)))
        val match = buildMatch(result = emptyList())

        overview.enterResult(match)

        assertThat(overview.entries.first { it.userId == uninvolvedPlayer }.attendancePoints).isEqualTo(5)
    }

    @Test
    fun `enterResult should not let uninvolved player points go below 0`() {
        val uninvolvedPlayer = UserId("uninvolved")
        val overview = PlayerOverview(groupId, mutableListOf(PlayerOverviewEntry(uninvolvedPlayer, attendancePoints = 0)))
        val match = buildMatch(result = emptyList())

        overview.enterResult(match)

        assertThat(overview.entries.first { it.userId == uninvolvedPlayer }.attendancePoints).isEqualTo(0)
    }

    @Test
    fun `enterResult should create new entry with 0 points for new absent cadre player`() {
        val player = UserId("newAbsentCadrePlayer")
        val overview = PlayerOverview(groupId) // kein Eintrag für diesen Spieler
        val match = buildMatch(cadre = listOf(player), result = emptyList())

        overview.enterResult(match)

        val entry = overview.entries.firstOrNull { it.userId == player }
        assertThat(entry).isNotNull()
        assertThat(entry!!.attendancePoints).isEqualTo(0)
        assertThat(entry.lastWaitingBenchMatchNumber).isNull()
    }

    @Test
    fun `enterResult should create new entry with 3 points and matchNumber for new waiting bench player`() {
        val player = UserId("newBenchPlayer")
        val matchNumber = MatchNumber(4)
        val overview = PlayerOverview(groupId) // kein Eintrag für diesen Spieler
        val match = buildMatch(waitingBench = listOf(player), result = emptyList(), matchNumber = matchNumber)

        overview.enterResult(match)

        val entry = overview.entries.firstOrNull { it.userId == player }
        assertThat(entry).isNotNull()
        assertThat(entry!!.attendancePoints).isEqualTo(3)
        assertThat(entry.lastWaitingBenchMatchNumber).isEqualTo(matchNumber)
    }

    @Test
    fun `enterResult should handle all cases correctly with many players`() {
        // Players
        val participating1 = UserId("participating1") // in cadre & result → +5
        val participating2 = UserId("participating2") // in cadre & result → +5
        val participating3 = UserId("participating3") // NOT in cadre, but in result → +5 (new entry)
        val absentCadre1 = UserId("absentCadre1")    // in cadre, NOT result → -3
        val absentCadre2 = UserId("absentCadre2")    // in cadre, NOT result → -3
        val benchPlayer1 = UserId("benchPlayer1")    // on waiting bench, NOT result → +3 + matchNumber
        val benchPlayer2 = UserId("benchPlayer2")    // on waiting bench, NOT result → +3 + matchNumber
        val uninvolved1 = UserId("uninvolved1")      // only in overview, not in match → -1
        val uninvolved2 = UserId("uninvolved2")      // only in overview, not in match → -1 (already 0 → stays 0)

        val matchNumber = MatchNumber(10)

        val overview =
            PlayerOverview(
                groupId,
                mutableListOf(
                    PlayerOverviewEntry(participating1, attendancePoints = 20, lastWaitingBenchMatchNumber = MatchNumber(3)),
                    PlayerOverviewEntry(participating2, attendancePoints = 0),
                    PlayerOverviewEntry(absentCadre1, attendancePoints = 15),
                    PlayerOverviewEntry(absentCadre2, attendancePoints = 2),   // 2 - 3 → capped at 0
                    PlayerOverviewEntry(benchPlayer1, attendancePoints = 8),
                    PlayerOverviewEntry(benchPlayer2, attendancePoints = 0),
                    PlayerOverviewEntry(uninvolved1, attendancePoints = 5),
                    PlayerOverviewEntry(uninvolved2, attendancePoints = 0),   // 0 - 1 → capped at 0
                ),
            )

        val match =
            buildMatch(
                cadre = listOf(participating1, participating2, absentCadre1, absentCadre2),
                waitingBench = listOf(benchPlayer1, benchPlayer2),
                result = listOf(participating1, participating2, participating3),
                matchNumber = matchNumber,
            )

        overview.enterResult(match)

        // Participating players already in overview → +5, clear lastWaitingBenchMatchNumber
        overview.entries.first { it.userId == participating1 }.also { entry ->
            assertThat(entry.attendancePoints).isEqualTo(25)
            assertThat(entry.lastWaitingBenchMatchNumber).isNull()
        }
        overview.entries.first { it.userId == participating2 }.also { entry ->
            assertThat(entry.attendancePoints).isEqualTo(5)
            assertThat(entry.lastWaitingBenchMatchNumber).isNull()
        }

        // New participating player (not in overview before) → entry created with +5
        overview.entries.first { it.userId == participating3 }.also { entry ->
            assertThat(entry.attendancePoints).isEqualTo(5)
            assertThat(entry.lastWaitingBenchMatchNumber).isNull()
        }

        // Absent cadre players → -3 (min 0)
        assertThat(overview.entries.first { it.userId == absentCadre1 }.attendancePoints).isEqualTo(12)
        assertThat(overview.entries.first { it.userId == absentCadre2 }.attendancePoints).isEqualTo(0)

        // Waiting bench players (not participating) → +3, lastWaitingBenchMatchNumber = matchNumber
        overview.entries.first { it.userId == benchPlayer1 }.also { entry ->
            assertThat(entry.attendancePoints).isEqualTo(11)
            assertThat(entry.lastWaitingBenchMatchNumber).isEqualTo(matchNumber)
        }
        overview.entries.first { it.userId == benchPlayer2 }.also { entry ->
            assertThat(entry.attendancePoints).isEqualTo(3)
            assertThat(entry.lastWaitingBenchMatchNumber).isEqualTo(matchNumber)
        }

        // Uninvolved players → -1 (min 0)
        assertThat(overview.entries.first { it.userId == uninvolved1 }.attendancePoints).isEqualTo(4)
        assertThat(overview.entries.first { it.userId == uninvolved2 }.attendancePoints).isEqualTo(0)
    }

    @Test
    fun `updateResult should give cadre player +8 when added to result`() {
        val player = UserId("cadrePlayer")
        val overview = PlayerOverview(groupId, mutableListOf(PlayerOverviewEntry(player, attendancePoints = 10)))
        val match = buildMatch(cadre = listOf(player))
        addedToResult(match, player)

        overview.updateResult(match)

        assertThat(overview.entries.first { it.userId == player }.attendancePoints).isEqualTo(18)
    }

    @Test
    fun `updateResult should give waiting bench player +2 and clear lastWaitingBenchMatchNumber when added to result`() {
        val player = UserId("benchPlayer")
        val overview =
            PlayerOverview(
                groupId,
                mutableListOf(PlayerOverviewEntry(player, attendancePoints = 10, lastWaitingBenchMatchNumber = MatchNumber(3))),
            )
        val match = buildMatch(waitingBench = listOf(player))
        addedToResult(match, player)

        overview.updateResult(match)

        overview.entries.first { it.userId == player }.also { entry ->
            assertThat(entry.attendancePoints).isEqualTo(12)
            assertThat(entry.lastWaitingBenchMatchNumber).isNull()
        }
    }

    @Test
    fun `updateResult should give uninvolved player +6 and clear lastWaitingBenchMatchNumber when added to result`() {
        val player = UserId("uninvolvedPlayer")
        val overview =
            PlayerOverview(
                groupId,
                mutableListOf(PlayerOverviewEntry(player, attendancePoints = 10, lastWaitingBenchMatchNumber = MatchNumber(2))),
            )
        val match = buildMatch()
        addedToResult(match, player)

        overview.updateResult(match)

        overview.entries.first { it.userId == player }.also { entry ->
            assertThat(entry.attendancePoints).isEqualTo(16)
            assertThat(entry.lastWaitingBenchMatchNumber).isNull()
        }
    }

    @Test
    fun `updateResult should create new entry when player added to result has no existing entry`() {
        val player = UserId("newPlayer")
        val overview = PlayerOverview(groupId)
        val match = buildMatch(cadre = listOf(player))
        addedToResult(match, player)

        overview.updateResult(match)

        val entry = overview.entries.firstOrNull { it.userId == player }
        assertThat(entry).isNotNull()
        assertThat(entry!!.attendancePoints).isEqualTo(8) // cadre: 3+5
        assertThat(entry.lastWaitingBenchMatchNumber).isNull()
    }

    @Test
    fun `updateResult should subtract 8 points from cadre player when removed from result`() {
        val player = UserId("cadrePlayer")
        val overview = PlayerOverview(groupId, mutableListOf(PlayerOverviewEntry(player, attendancePoints = 20)))
        val match = buildMatch(cadre = listOf(player))
        removedFromResult(match, player)

        overview.updateResult(match)

        assertThat(overview.entries.first { it.userId == player }.attendancePoints).isEqualTo(12)
    }

    @Test
    fun `updateResult should give waiting bench player -2 and set lastWaitingBenchMatchNumber when removed from result`() {
        val player = UserId("benchPlayer")
        val matchNumber = MatchNumber(6)
        val overview = PlayerOverview(groupId, mutableListOf(PlayerOverviewEntry(player, attendancePoints = 10)))
        val match = buildMatch(waitingBench = listOf(player), matchNumber = matchNumber)
        removedFromResult(match, player)

        overview.updateResult(match)

        overview.entries.first { it.userId == player }.also { entry ->
            assertThat(entry.attendancePoints).isEqualTo(8)
            assertThat(entry.lastWaitingBenchMatchNumber).isEqualTo(matchNumber)
        }
    }

    @Test
    fun `updateResult should subtract 6 points from uninvolved player when removed from result`() {
        val player = UserId("uninvolvedPlayer")
        val overview = PlayerOverview(groupId, mutableListOf(PlayerOverviewEntry(player, attendancePoints = 20)))
        val match = buildMatch()
        removedFromResult(match, player)

        overview.updateResult(match)

        assertThat(overview.entries.first { it.userId == player }.attendancePoints).isEqualTo(14)
    }

    @Test
    fun `updateResult should cap points at 0 when removed from result`() {
        val player = UserId("cadrePlayer")
        val overview = PlayerOverview(groupId, mutableListOf(PlayerOverviewEntry(player, attendancePoints = 3)))
        val match = buildMatch(cadre = listOf(player))
        removedFromResult(match, player)

        overview.updateResult(match)

        assertThat(overview.entries.first { it.userId == player }.attendancePoints).isEqualTo(0)
    }

    @Test
    fun `updateResult should handle all cases correctly with many players`() {
        val cadreAdded = UserId("cadreAdded")         // in cadre, added to result      → +8
        val benchAdded = UserId("benchAdded")         // on bench, added to result       → +2, clear waitingBenchNumber
        val otherAdded = UserId("otherAdded")         // uninvolved, added to result     → +6, clear waitingBenchNumber
        val newAdded = UserId("newAdded")             // new player, cadre, added        → new entry +8
        val cadreRemoved = UserId("cadreRemoved")     // in cadre, removed from result   → -8
        val benchRemoved = UserId("benchRemoved")     // on bench, removed from result   → -2, set waitingBenchNumber
        val otherRemoved = UserId("otherRemoved")     // uninvolved, removed from result → -6

        val matchNumber = MatchNumber(9)

        val overview =
            PlayerOverview(
                groupId,
                mutableListOf(
                    PlayerOverviewEntry(cadreAdded, attendancePoints = 10),
                    PlayerOverviewEntry(benchAdded, attendancePoints = 10, lastWaitingBenchMatchNumber = MatchNumber(2)),
                    PlayerOverviewEntry(otherAdded, attendancePoints = 10, lastWaitingBenchMatchNumber = MatchNumber(1)),
                    // newAdded has no existing entry
                    PlayerOverviewEntry(cadreRemoved, attendancePoints = 20),
                    PlayerOverviewEntry(benchRemoved, attendancePoints = 15),
                    PlayerOverviewEntry(otherRemoved, attendancePoints = 18),
                ),
            )

        val match =
            buildMatch(
                cadre = listOf(cadreAdded, cadreRemoved, newAdded),
                waitingBench = listOf(benchAdded, benchRemoved),
                matchNumber = matchNumber,
            )

        addedToResult(match, cadreAdded)
        addedToResult(match, benchAdded)
        addedToResult(match, otherAdded)
        addedToResult(match, newAdded)
        removedFromResult(match, cadreRemoved)
        removedFromResult(match, benchRemoved)
        removedFromResult(match, otherRemoved)

        overview.updateResult(match)

        // Added to result
        assertThat(overview.entries.first { it.userId == cadreAdded }.attendancePoints).isEqualTo(18)
        overview.entries.first { it.userId == benchAdded }.also { e ->
            assertThat(e.attendancePoints).isEqualTo(12)
            assertThat(e.lastWaitingBenchMatchNumber).isNull()
        }
        overview.entries.first { it.userId == otherAdded }.also { e ->
            assertThat(e.attendancePoints).isEqualTo(16)
            assertThat(e.lastWaitingBenchMatchNumber).isNull()
        }
        overview.entries.first { it.userId == newAdded }.also { e ->
            assertThat(e.attendancePoints).isEqualTo(8)
            assertThat(e.lastWaitingBenchMatchNumber).isNull()
        }

        // Removed from result
        assertThat(overview.entries.first { it.userId == cadreRemoved }.attendancePoints).isEqualTo(12)
        overview.entries.first { it.userId == benchRemoved }.also { e ->
            assertThat(e.attendancePoints).isEqualTo(13)
            assertThat(e.lastWaitingBenchMatchNumber).isEqualTo(matchNumber)
        }
        assertThat(overview.entries.first { it.userId == otherRemoved }.attendancePoints).isEqualTo(12)
    }

    private fun buildMatch(
        cadre: List<UserId> = emptyList(),
        waitingBench: List<UserId> = emptyList(),
        result: List<UserId> = emptyList(),
        matchNumber: MatchNumber = MatchNumber(5),
    ): MatchAggregate =
        MatchAggregate("matchId").also { m ->
            m.groupId = groupId
            m.matchNumber = matchNumber
            m.playerCount = PlayerCount(MinPlayer(4), MaxPlayer(12))
            cadre.forEach { userId ->
                m.cadre.add(
                    RegisteredPlayer.MainPlayer(userId, 0, LocalDateTime.now(), RegistrationStatus.Registered, 0),
                )
            }
            waitingBench.forEach { userId ->
                m.waitingBench.add(
                    RegisteredPlayer.MainPlayer(userId, 0, LocalDateTime.now(), RegistrationStatus.Registered, 0),
                )
            }
            m.result =
                result.map { userId ->
                    ParticipatingPlayer(userId, PlayerResult.WIN, MatchTeam.A)
                }
        }

    private fun addedToResult(
        match: MatchAggregate,
        userId: UserId,
    ) = match.apply(
        MatchResultUpdatedEvent(
            aggregateId = match.aggregateId,
            groupId = match.groupId,
            user = userId,
            matchNumber = match.matchNumber,
            oldResult = null,
            oldTeam = null,
            newResult = PlayerResult.WIN,
            newTeam = MatchTeam.A,
        ),
    )

    private fun removedFromResult(
        match: MatchAggregate,
        userId: UserId,
    ) = match.apply(
        MatchResultUpdatedEvent(
            aggregateId = match.aggregateId,
            groupId = match.groupId,
            user = userId,
            matchNumber = match.matchNumber,
            oldResult = PlayerResult.WIN,
            oldTeam = MatchTeam.A,
            newResult = null,
            newTeam = null,
        ),
    )
}
