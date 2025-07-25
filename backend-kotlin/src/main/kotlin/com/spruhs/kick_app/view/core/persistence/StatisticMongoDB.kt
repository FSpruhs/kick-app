package com.spruhs.kick_app.view.core.persistence


import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.view.core.service.PlayerStatisticProjection
import com.spruhs.kick_app.view.core.service.StatisticProjectionRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Document(collection = "player-statistics")
data class PlayerStatisticDocument(
    @Id
    val id: String,
    val groupId: String,
    val userId: String,
    val totalMatches: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
)

@Service
class StatisticProjectionMongoDB(
    private val repository: StatisticsRepository
) : StatisticProjectionRepository {
    override suspend fun findByPlayer(
        groupId: GroupId,
        userId: UserId
    ): PlayerStatisticProjection? = repository.findByGroupIdAndUserId(groupId.value, userId.value)
        .awaitFirstOrNull()
        ?.toProjection()

    override suspend fun save(statistic: PlayerStatisticProjection) {
        repository.save(statistic.toDocument()).awaitSingle()
    }
}

@Repository
interface StatisticsRepository : ReactiveMongoRepository<PlayerStatisticDocument, String> {
    fun findByGroupIdAndUserId(groupId: String, userId: String): Mono<PlayerStatisticDocument>
}

private fun PlayerStatisticDocument.toProjection() = PlayerStatisticProjection(
    id = id,
    groupId = GroupId(groupId),
    userId = UserId(userId),
    totalMatches = totalMatches,
    wins = wins,
    losses = losses,
    draws = draws,
)

private fun PlayerStatisticProjection.toDocument() = PlayerStatisticDocument(
    id = id,
    groupId = groupId.value,
    userId = userId.value,
    totalMatches = totalMatches,
    wins = wins,
    losses = losses,
    draws = draws,
)