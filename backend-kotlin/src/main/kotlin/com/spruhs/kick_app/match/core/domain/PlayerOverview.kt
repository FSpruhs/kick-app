package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.match.api.MatchResultUpdatedEvent

class PlayerOverview(
    val groupId: GroupId,
    val entries: MutableList<PlayerOverviewEntry> = mutableListOf(),
) {
    fun enterResult(match: MatchAggregate) {
        val updatedEntries = entries.toMutableList()
        val affectedUserIds = mutableSetOf<UserId>()

        val participatingUserIds = match.result.map { it.userId }.toSet()

        for (userId in participatingUserIds) {
            affectedUserIds.add(userId)
            val index = updatedEntries.indexOfFirst { it.userId == userId }
            if (index >= 0) {
                val entry = updatedEntries[index]
                updatedEntries[index] =
                    entry.copy(
                        attendancePoints = entry.attendancePoints + 5,
                        lastWaitingBenchMatchNumber = null,
                    )
            } else {
                updatedEntries.add(PlayerOverviewEntry(userId, 5, null))
            }
        }

        val cadreUserIds =
            match.cadre
                .filterIsInstance<RegisteredPlayer.MainPlayer>()
                .map { it.userId }
                .toSet()
        for (userId in cadreUserIds) {
            if (userId in participatingUserIds) continue
            affectedUserIds.add(userId)
            val index = updatedEntries.indexOfFirst { it.userId == userId }
            if (index >= 0) {
                val entry = updatedEntries[index]
                updatedEntries[index] =
                    entry.copy(
                        attendancePoints = maxOf(0, entry.attendancePoints - 3),
                    )
            } else {
                updatedEntries.add(PlayerOverviewEntry(userId, 0, null))
            }
        }

        val waitingBenchUserIds =
            match.waitingBench
                .filterIsInstance<RegisteredPlayer.MainPlayer>()
                .map { it.userId }
                .toSet()
        for (userId in waitingBenchUserIds) {
            if (userId in participatingUserIds) continue
            affectedUserIds.add(userId)
            val index = updatedEntries.indexOfFirst { it.userId == userId }
            if (index >= 0) {
                val entry = updatedEntries[index]
                updatedEntries[index] =
                    entry.copy(
                        attendancePoints = entry.attendancePoints + 3,
                        lastWaitingBenchMatchNumber = match.matchNumber,
                    )
            } else {
                updatedEntries.add(PlayerOverviewEntry(userId, 3, match.matchNumber))
            }
        }

        for (i in updatedEntries.indices) {
            val entry = updatedEntries[i]
            if (entry.userId !in affectedUserIds) {
                updatedEntries[i] =
                    entry.copy(
                        attendancePoints = maxOf(0, entry.attendancePoints - 1),
                    )
            }
        }

        entries.clear()
        entries.addAll(updatedEntries)
    }

    fun updateResult(match: MatchAggregate) {
        val events = match.changes.filterIsInstance<MatchResultUpdatedEvent>()
        val updatedEntries = entries.toMutableList()

        val cadreUserIds =
            match.cadre
                .filterIsInstance<RegisteredPlayer.MainPlayer>()
                .map { it.userId }
                .toSet()
        val waitingBenchUserIds =
            match.waitingBench
                .filterIsInstance<RegisteredPlayer.MainPlayer>()
                .map { it.userId }
                .toSet()

        for (event in events) {
            when {
                event.oldResult == null && event.newResult != null -> {
                    val correction =
                        when (event.user) {
                            in cadreUserIds -> 3 + 5
                            in waitingBenchUserIds -> -3 + 5
                            else -> 1 + 5
                        }
                    val index = updatedEntries.indexOfFirst { it.userId == event.user }
                    if (index >= 0) {
                        val entry = updatedEntries[index]
                        updatedEntries[index] =
                            entry.copy(
                                attendancePoints = maxOf(0, entry.attendancePoints + correction),
                                lastWaitingBenchMatchNumber = null,
                            )
                    } else {
                        updatedEntries.add(PlayerOverviewEntry(event.user, maxOf(0, correction), null))
                    }
                }

                event.oldResult != null && event.newResult == null -> {
                    val correction =
                        when (event.user) {
                            in cadreUserIds -> -5 - 3
                            in waitingBenchUserIds -> -5 + 3
                            else -> -5 - 1
                        }
                    val index = updatedEntries.indexOfFirst { it.userId == event.user }
                    if (index >= 0) {
                        val entry = updatedEntries[index]
                        val newWaitingBenchNumber = if (event.user in waitingBenchUserIds) match.matchNumber else entry.lastWaitingBenchMatchNumber
                        updatedEntries[index] =
                            entry.copy(
                                attendancePoints = maxOf(0, entry.attendancePoints + correction),
                                lastWaitingBenchMatchNumber = newWaitingBenchNumber,
                            )
                    }
                }
                else -> Unit
            }
        }

        entries.clear()
        entries.addAll(updatedEntries)
    }
}

data class PlayerOverviewEntry(
    val userId: UserId,
    val attendancePoints: Int = 0,
    val lastWaitingBenchMatchNumber: MatchNumber? = null,
)

interface PlayerOverviewPersistencePort {
    suspend fun getOverview(groupId: GroupId): PlayerOverview?

    suspend fun save(overview: PlayerOverview)
}
