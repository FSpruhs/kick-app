package com.spruhs.kick_app.match.core.application

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.match.core.domain.PlayerOverview
import com.spruhs.kick_app.match.core.domain.PlayerOverviewPersistencePort
import org.springframework.stereotype.Service

@Service
class PlayerOverviewService(
    private val playerOverviewPersistencePort: PlayerOverviewPersistencePort,
) {
    suspend fun getOverview(groupId: GroupId): PlayerOverview =
        playerOverviewPersistencePort.getOverview(groupId) ?: PlayerOverview(groupId = groupId)

    suspend fun save(overview: PlayerOverview) {
        playerOverviewPersistencePort.save(overview)
    }

    suspend fun getOverviewEntry(
        groupId: GroupId,
        userId: UserId,
    ) = getOverview(groupId).entries.firstOrNull { it.userId == userId }
}
