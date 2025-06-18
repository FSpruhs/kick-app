package com.spruhs.kick_app.viewservice.core.persistence

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.Result
import com.spruhs.kick_app.viewservice.core.service.MatchProjection
import com.spruhs.kick_app.viewservice.core.service.MatchProjectionRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Service
class MatchProjectionMongoDB(
    private val repository: MatchRepository
) : MatchProjectionRepository {
    override suspend fun save(matchProjection: MatchProjection) {
        repository.save(matchProjection.toDocument()).awaitSingle()
    }

    override suspend  fun findById(matchId: MatchId): MatchProjection? =
        repository.findById(matchId.value)
            .awaitFirstOrNull()
            ?.toProjection()

    override suspend fun findAllByGroupId(groupId: GroupId): List<MatchProjection> =
        repository.findByGroupId(groupId.value).collectList()
            .awaitSingle()
            .map { it.toProjection() }

}

@Document(collection = "matches")
data class MatchDocument(
    @Id
    val id: String,
    val groupId: String,
    val start: LocalDateTime,
    var playground: String?,
    val maxPlayer: Int,
    val minPlayer: Int,
    var isCanceled: Boolean,
    var cadrePlayers: Set<String>,
    var deregisteredPlayers: Set<String>,
    var waitingBenchPlayers: Set<String>,
    var teamA: Set<String>,
    var teamB: Set<String>,
    var result: String?
)

@Repository
interface MatchRepository : ReactiveMongoRepository<MatchDocument, String> {
    fun findByGroupId(groupId: String): Flux<MatchDocument>
}

private fun MatchDocument.toProjection() = MatchProjection(
    id = MatchId(id),
    groupId = GroupId(groupId),
    start = start,
    playground = playground,
    isCanceled = this.isCanceled,
    maxPlayer = maxPlayer,
    minPlayer = minPlayer,
    result = result?.let { Result.valueOf(it) },
    cadrePlayers = cadrePlayers.map { UserId(it) }.toSet(),
    waitingBenchPlayers = waitingBenchPlayers.map { UserId(it) }.toSet(),
    deregisteredPlayers = deregisteredPlayers.map { UserId(it) }.toSet(),
    teamA = teamA.map { UserId(it) }.toSet(),
    teamB = teamB.map { UserId(it) }.toSet(),
)

private fun MatchProjection.toDocument() = MatchDocument(
    id = id.value,
    groupId = groupId.value,
    start = start,
    playground = playground,
    maxPlayer = maxPlayer,
    minPlayer = minPlayer,
    isCanceled = isCanceled,
    cadrePlayers = cadrePlayers.map { it.value }.toSet(),
    deregisteredPlayers = deregisteredPlayers.map { it.value }.toSet(),
    waitingBenchPlayers = waitingBenchPlayers.map { it.value }.toSet(),
    teamA = teamA.map { it.value }.toSet(),
    teamB = teamB.map { it.value }.toSet(),
    result = result?.name
)