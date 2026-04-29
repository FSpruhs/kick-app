package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.match.api.MatchResultUpdatedEvent
import com.spruhs.kick_app.match.api.PlayerOverviewEntry

class PlayerOverview(
    val groupId: GroupId,
    val entries: MutableList<PlayerOverviewEntry> = mutableListOf(),
) {
    companion object {
        const val PARTICIPATION_POINTS = 5
        const val BENCH_BONUS = 3
        const val CADRE_PENALTY = 3
        const val UNINVOLVED_PENALTY = 1
    }

    fun enterResult(match: MatchAggregate) {
        val updatedEntries = entries.toMutableList()
        val affectedUserIds = mutableSetOf<UserId>()
        val participatingUserIds = match.result.map { it.userId }.toSet()

        calculateParticipatingPlayers(participatingUserIds, affectedUserIds, updatedEntries)
        calculateCadrePlayers(match, participatingUserIds, affectedUserIds, updatedEntries)
        calculateWaitingBenchPlayers(match, participatingUserIds, affectedUserIds, updatedEntries)
        reduceRemainingPlayers(updatedEntries, affectedUserIds)

        entries.clear()
        entries.addAll(updatedEntries)
    }

    fun updateResult(match: MatchAggregate) {
        val updatedEntries = entries.toMutableList()
        val cadreUserIds = getCadreUserIds(match)
        val waitingBenchUserIds = getWaitingBenchUserIds(match)

        for (event in resultUpdatedEvents(match)) {
            when {
                isNewPlayerInResult(event) -> handleNewPlayer(event, cadreUserIds, waitingBenchUserIds, updatedEntries)
                isPlayerLeavingResult(event) -> handlePlayerLeaved(event, cadreUserIds, waitingBenchUserIds, updatedEntries, match)
                else -> Unit
            }
        }

        entries.clear()
        entries.addAll(updatedEntries)
    }

    private fun calculateParticipatingPlayers(participatingUserIds: Set<UserId>, affectedUserIds: MutableSet<UserId>, updatedEntries: MutableList<PlayerOverviewEntry>) {
        for (userId in participatingUserIds) {
            affectedUserIds.add(userId)
            updateOrAddEntry(
                updatedEntries,
                userId,
                update = { it.copy(attendancePoints = it.attendancePoints + PARTICIPATION_POINTS, lastWaitingBenchMatchNumber = null) },
                default = { PlayerOverviewEntry(userId, PARTICIPATION_POINTS, null) },
            )
        }
    }

    private fun calculateCadrePlayers(match: MatchAggregate, participatingUserIds: Set<UserId>, affectedUserIds: MutableSet<UserId>, updatedEntries: MutableList<PlayerOverviewEntry>) {
        val cadreUserIds = getCadreUserIds(match)
        for (userId in cadreUserIds) {
            if (userId in participatingUserIds) continue
            affectedUserIds.add(userId)
            updateOrAddEntry(
                updatedEntries,
                userId,
                update = { it.copy(attendancePoints = maxOf(0, it.attendancePoints - CADRE_PENALTY)) },
                default = { PlayerOverviewEntry(userId, 0, null) },
            )
        }
    }

    private fun calculateWaitingBenchPlayers(match: MatchAggregate, participatingUserIds: Set<UserId>, affectedUserIds: MutableSet<UserId>, updatedEntries: MutableList<PlayerOverviewEntry>) {
        val waitingBenchUserIds = getWaitingBenchUserIds(match)
        for (userId in waitingBenchUserIds) {
            if (userId in participatingUserIds) continue
            affectedUserIds.add(userId)
            updateOrAddEntry(
                updatedEntries,
                userId,
                update = { it.copy(attendancePoints = it.attendancePoints + BENCH_BONUS, lastWaitingBenchMatchNumber = match.matchNumber) },
                default = { PlayerOverviewEntry(userId, BENCH_BONUS, match.matchNumber) },
            )
        }
    }

    private fun reduceRemainingPlayers(updatedEntries: MutableList<PlayerOverviewEntry>, affectedUserIds: Set<UserId>) {
        for (i in updatedEntries.indices) {
            val entry = updatedEntries[i]
            if (entry.userId !in affectedUserIds) {
                updatedEntries[i] =
                    entry.copy(
                        attendancePoints = maxOf(0, entry.attendancePoints - 1),
                    )
            }
        }
    }

    private fun resultUpdatedEvents(match: MatchAggregate): List<MatchResultUpdatedEvent> =
        match.changes.filterIsInstance<MatchResultUpdatedEvent>()

    private fun isPlayerLeavingResult(event: MatchResultUpdatedEvent): Boolean = event.oldResult != null && event.newResult == null

    private fun isNewPlayerInResult(event: MatchResultUpdatedEvent): Boolean = event.oldResult == null && event.newResult != null

    private fun handlePlayerLeaved(
        event: MatchResultUpdatedEvent,
        cadreUserIds: Set<UserId>,
        waitingBenchUserIds: Set<UserId>,
        updatedEntries: MutableList<PlayerOverviewEntry>,
        match: MatchAggregate,
    ) {
        val correction =
            when (event.user) {
                in cadreUserIds -> -PARTICIPATION_POINTS - CADRE_PENALTY
                in waitingBenchUserIds -> -PARTICIPATION_POINTS + BENCH_BONUS
                else -> -PARTICIPATION_POINTS - UNINVOLVED_PENALTY
            }
        updateOrAddEntry(
            updatedEntries,
            event.user,
            update = { entry ->
                val newWaitingBenchNumber = if (event.user in waitingBenchUserIds) match.matchNumber else entry.lastWaitingBenchMatchNumber
                entry.copy(attendancePoints = maxOf(0, entry.attendancePoints + correction), lastWaitingBenchMatchNumber = newWaitingBenchNumber)
            },
            default = null,
        )
    }

    private fun handleNewPlayer(
        event: MatchResultUpdatedEvent,
        cadreUserIds: Set<UserId>,
        waitingBenchUserIds: Set<UserId>,
        updatedEntries: MutableList<PlayerOverviewEntry>,
    ) {
        val correction =
            when (event.user) {
                in cadreUserIds -> CADRE_PENALTY + PARTICIPATION_POINTS
                in waitingBenchUserIds -> -BENCH_BONUS + PARTICIPATION_POINTS
                else -> UNINVOLVED_PENALTY + PARTICIPATION_POINTS
            }
        updateOrAddEntry(
            updatedEntries,
            event.user,
            update = { it.copy(attendancePoints = maxOf(0, it.attendancePoints + correction), lastWaitingBenchMatchNumber = null) },
            default = { PlayerOverviewEntry(event.user, maxOf(0, correction), null) },
        )
    }

    private fun updateOrAddEntry(
        entries: MutableList<PlayerOverviewEntry>,
        userId: UserId,
        update: (PlayerOverviewEntry) -> PlayerOverviewEntry,
        default: (() -> PlayerOverviewEntry)?,
    ) {
        val index = entries.indexOfFirst { it.userId == userId }
        if (index >= 0) {
            entries[index] = update(entries[index])
        } else if (default != null) {
            entries.add(default())
        }
    }

    private fun getCadreUserIds(match: MatchAggregate): Set<UserId> =
        match.cadre
            .filterIsInstance<RegisteredPlayer.MainPlayer>()
            .map { it.userId }
            .toSet()

    private fun getWaitingBenchUserIds(match: MatchAggregate): Set<UserId> =
        match.waitingBench
            .filterIsInstance<RegisteredPlayer.MainPlayer>()
            .map { it.userId }
            .toSet()
}

interface PlayerOverviewPersistencePort {
    suspend fun getOverview(groupId: GroupId): PlayerOverview?

    suspend fun save(overview: PlayerOverview)
}
