package com.spruhs.kick_app.view.core.service

import com.mongodb.client.model.search.TotalSearchCount
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.group.core.domain.PlayerNotFoundException
import com.spruhs.kick_app.view.api.GroupApi
import org.springframework.stereotype.Service

@Service
class StatisticService(
    private val repository: StatisticProjectionRepository,
    private val groupApi: GroupApi
) {

    suspend fun getPlayerStatistics(groupId: GroupId, userId: UserId, requestingUserId: UserId): PlayerStatisticProjection {
        require(groupApi.isActiveMember(groupId, requestingUserId)) {
            throw UserNotAuthorizedException(requestingUserId)
        }

        return repository.findByPlayer(groupId, userId) ?: throw PlayerNotFoundException(userId)
    }
}

interface StatisticProjectionRepository {
    suspend fun findByPlayer(groupId: GroupId, userId: UserId): PlayerStatisticProjection?
    suspend fun save(statistic: PlayerStatisticProjection)
}

data class PlayerStatisticProjection(
    val id: String,
    val groupId: GroupId,
    val userId: UserId,
    val totalMatches: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
)