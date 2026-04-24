package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.match.api.ParticipatingPlayer

class PlayerOverview(
    val groupId: GroupId,
    val entries: List<PlayerOverviewEntry>,
    ) {

    fun processMatchResult(match: MatchAggregate, participatingPlayers: List<ParticipatingPlayer>): Int {
        return entries.sumOf { it.attendancePoints }
    }
}

data class PlayerOverviewEntry(
    val userId: UserId,
    val attendancePoints: Int,
    val lastWaitingBenchMatchNumber: MatchNumber,
)

interface PlayerOverviewPersistencePort {
    suspend fun getOverview(groupId: GroupId): PlayerOverview

    suspend fun save(overview: PlayerOverview)
}
