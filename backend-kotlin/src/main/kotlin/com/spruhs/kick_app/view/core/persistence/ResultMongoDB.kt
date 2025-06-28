package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.MatchTeam
import com.spruhs.kick_app.common.PlayerResult
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.view.core.service.PlayerResultProjection
import com.spruhs.kick_app.view.core.service.ResultProjection
import com.spruhs.kick_app.view.core.service.ResultProjectionRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Document(collection = "player-results")
data class ResultDocument(
    @Id
    val id: String,
    val matchId: String,
    val players: Map<String, PlayerResultDocument> = emptyMap(),
)

data class PlayerResultDocument(
    val result: PlayerResult,
    val team: MatchTeam
)

@Service
class ResultProjectionMongoDB(
    private val repository: ResultRepository
) : ResultProjectionRepository {
    override suspend fun findByMatchId(matchId: MatchId): ResultProjection? {
        return repository.findByMatchId(matchId.value)
            .map { it.toProjection() }
            .awaitFirstOrNull()
    }

    override suspend fun save(result: ResultProjection) {
        repository.save(result.toDocument()).awaitSingle()
    }
}

@Repository
interface ResultRepository : ReactiveMongoRepository<ResultDocument, String> {
    fun findByMatchId(matchId: String): Mono<ResultDocument>
}

private fun ResultProjection.toDocument(): ResultDocument =
    ResultDocument(
        id = this.id,
        matchId = this.matchId.value,
        players = this.players.mapKeys { (key, _) -> key.value }
            .mapValues { (_, value) -> value.toDocument() }
    )

private fun PlayerResultProjection.toDocument(): PlayerResultDocument =
    PlayerResultDocument(
        result = this.matchResult,
        team = this.team
    )

private fun ResultDocument.toProjection(): ResultProjection =
    ResultProjection(
        id = this.id,
        matchId = MatchId(this.matchId),
        players = this.players.mapKeys { (key, _) -> UserId(key) }
            .mapValues { (_, value) -> value.toProjection() }
    )

private fun PlayerResultDocument.toProjection(): PlayerResultProjection =
    PlayerResultProjection(
        matchResult = this.result,
        team = this.team
    )
