package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.match.api.MatchNumber
import com.spruhs.kick_app.match.api.MatchNumberChangedEvent
import java.time.LocalDateTime

data class MatchOverviewEntry(
    val matchId: MatchId,
    val matchNumber: MatchNumber,
    val start: LocalDateTime,
    val state: MatchState,
)

enum class MatchState {
    PLANNED,
    FINISHED,
    RESULT_ENTERED,
}

class MatchOverview(
    val groupId: GroupId,
    val entries: MutableList<MatchOverviewEntry> = mutableListOf(),
    val events: MutableList<BaseEvent> = mutableListOf(),
) {
    fun add(
        matchId: MatchId,
        start: LocalDateTime,
    ): MatchNumber {
        require(start.isAfter(LocalDateTime.now())) { "Match start must be in the future" }
        require(entries.map { it.matchId }.none { it == matchId }) { "Match with id ${matchId.value} already exists" }
        return nextMatchNumber(start).also {
            increaseOtherMatchNumbers(it)
            entries.add(MatchOverviewEntry(matchId, it, start, MatchState.PLANNED))
            entries.sortBy { entry -> entry.start }
        }
    }

    fun cancel(matchId: MatchId) {
        val entry = entries.find { it.matchId == matchId } ?: return
        require(entry.start.isAfter(LocalDateTime.now())) {
            "Match with id ${matchId.value} has already started or is in the past"
        }
        entries.remove(entry)
        decreaseOtherMatchNumbers(entry)
    }

    fun resultEntered(matchId: MatchId) {
        val entry =
            entries.find { it.matchId == matchId }
                ?: return

        entries.filter { it.matchNumber < entry.matchNumber }
            .filter { it.state == MatchState.PLANNED }
            .forEach { planned ->
                entries.replaceAll { e ->
                    if (e.matchId == planned.matchId) e.copy(state = MatchState.FINISHED) else e
                }
            }

        entries.removeIf { it.matchNumber < entry.matchNumber && it.state == MatchState.RESULT_ENTERED }

        entries.replaceAll { e ->
            if (e.matchId == matchId) e.copy(state = MatchState.RESULT_ENTERED) else e
        }
    }

    private fun nextMatchNumber(start: LocalDateTime): MatchNumber {
        if (entries.isEmpty()) return MatchNumber(1)
        val sorted = entries.sortedWith(compareBy({ it.start }, { it.matchNumber }))
        val insertIndex = sorted.indexOfFirst { it.start.isAfter(start) }
        return if (insertIndex == -1) {
            (sorted.lastOrNull()?.matchNumber ?: MatchNumber(0)) + 1
        } else {
            val prevNumber = if (insertIndex == 0) MatchNumber(0) else sorted[insertIndex - 1].matchNumber
            prevNumber + 1
        }
    }

    private fun decreaseOtherMatchNumbers(entry: MatchOverviewEntry) =
        entries.replaceAll { e ->
            if (e.matchNumber > entry.matchNumber) {
                val newMatchNumber = e.matchNumber - 1
                events.add(MatchNumberChangedEvent(e.matchId.value, newMatchNumber))
                e.copy(matchNumber = newMatchNumber)
            } else {
                e
            }
        }

    private fun increaseOtherMatchNumbers(newNumber: MatchNumber) =
        entries.replaceAll { entry ->
            if (entry.matchNumber >= newNumber) {
                val newMatchId = entry.matchNumber + 1
                events.add(MatchNumberChangedEvent(entry.matchId.value, newMatchId))
                entry.copy(matchNumber = newMatchId)
            } else {
                entry
            }
        }
}

interface MatchOverviewPersistencePort {
    suspend fun getOverview(groupId: GroupId): MatchOverview?

    suspend fun save(overview: MatchOverview)
}
