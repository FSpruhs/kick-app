package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.match.api.ParticipatingPlayer

class PlayerMatchOverview(
    private val groupId: GroupId,
    private val entries: List<PlayerMatchOverviewEntry>,
    ) {

    fun processMatchResult(match: MatchAggregate, participatingPlayers: List<ParticipatingPlayer>): Int {
        return entries.sumOf { it.attendancePoints }
    }
}

data class PlayerMatchOverviewEntry(
    val userId: UserId,
    val attendancePoints: Int,
    val lastWaitingBenchMatchNumber: MatchNumber
)
