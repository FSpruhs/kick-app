package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.match.api.MatchTeam
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.api.PlayerResult
import com.spruhs.kick_app.view.core.service.MatchFilter
import com.spruhs.kick_app.view.core.service.MatchProjection
import com.spruhs.kick_app.view.core.service.MatchProjectionRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Document(collection = "matches")
data class MatchDocument(
    @Id
    val id: String,
    val groupId: String,
    val start: LocalDateTime,
    var playground: String?,
    val maxPlayer: Int,
    val minPlayer: Int,
    var canceled: Boolean,
    var cadrePlayers: Set<String>,
    var deregisteredPlayers: Set<String>,
    var waitingBenchPlayers: Set<String>,
    var result: List<MatchResultDocument>
)

data class MatchResultDocument(
    val userId: String,
    val team: String,
    val result: String,
)

@Service
class MatchProjectionMongoDB(
    private val repository: MatchRepository
) : MatchProjectionRepository {
    override suspend fun save(matchProjection: MatchProjection) {
        repository.save(matchProjection.toDocument()).awaitSingle()
    }

    override suspend fun findById(matchId: MatchId): MatchProjection? =
        repository.findById(matchId.value)
            .awaitFirstOrNull()
            ?.toProjection()

    override suspend fun findAllByGroupId(
        groupId: GroupId,
        filter: MatchFilter
    ): List<MatchProjection> = repository.findFilteredMatches(groupId.value, filter)
        .map { it.toProjection() }
        .collectList()
        .awaitSingle()
}

@Repository
interface MatchRepository : ReactiveMongoRepository<MatchDocument, String>, MatchRepositoryCustom

interface MatchRepositoryCustom {
    fun findFilteredMatches(groupId: String, filter: MatchFilter): Flux<MatchDocument>
}

class MatchRepositoryImpl(
    private val mongoTemplate: ReactiveMongoTemplate,
) : MatchRepositoryCustom {
    override fun findFilteredMatches(groupId: String, filter: MatchFilter): Flux<MatchDocument> {

        val criteria = buildList {
            add(Criteria.where(MatchDocument::groupId.name).`is`(groupId))
            add(Criteria.where(MatchDocument::canceled.name).`is`(false))
            filter.after?.let { add(Criteria.where(MatchDocument::start.name).gte(it)) }
            filter.before?.let { add(Criteria.where(MatchDocument::start.name).lte(it)) }
        }

        val query = Query().apply {
            if (criteria.isNotEmpty()) {
                addCriteria(Criteria().andOperator(*criteria.toTypedArray()))
            }
            with(Sort.by(Sort.Direction.DESC, MatchDocument::start.name))
            filter.limit?.let { limit(it) }
        }

        return mongoTemplate.find(query, MatchDocument::class.java)
    }
}

private fun MatchDocument.toProjection() = MatchProjection(
    id = MatchId(id),
    groupId = GroupId(groupId),
    start = start,
    playground = playground,
    isCanceled = this.canceled,
    maxPlayer = maxPlayer,
    minPlayer = minPlayer,
    result = result.map {
        ParticipatingPlayer(
            userId = UserId(it.userId),
            playerResult = PlayerResult.valueOf(it.result),
            team = MatchTeam.valueOf(it.team)
        )
    },
    cadrePlayers = cadrePlayers.map { UserId(it) }.toSet(),
    waitingBenchPlayers = waitingBenchPlayers.map { UserId(it) }.toSet(),
    deregisteredPlayers = deregisteredPlayers.map { UserId(it) }.toSet(),
)

private fun MatchProjection.toDocument() = MatchDocument(
    id = id.value,
    groupId = groupId.value,
    start = start,
    playground = playground,
    maxPlayer = maxPlayer,
    minPlayer = minPlayer,
    canceled = isCanceled,
    cadrePlayers = cadrePlayers.map { it.value }.toSet(),
    deregisteredPlayers = deregisteredPlayers.map { it.value }.toSet(),
    waitingBenchPlayers = waitingBenchPlayers.map { it.value }.toSet(),
    result = result.map {
        MatchResultDocument(it.userId.value, it.team.name, it.playerResult.name)
    }
)