package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.Result
import com.spruhs.kick_app.common.UserId
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

@Document(collection = "results")
data class ResultDocument(
    @Id
    val id: String,
    val matchId: String,
    val result: String,
    val teamA: List<String>,
    val teamB: List<String>,
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

private fun ResultProjection.toDocument(): ResultDocument = ResultDocument(
    id = this.id,
    matchId = this.matchId.value,
    result = this.result.name,
    teamA = this.teamA.map { it.value },
    teamB = this.teamB.map { it.value },
)


private fun ResultDocument.toProjection(): ResultProjection = ResultProjection(
    id = this.id,
    matchId = MatchId(this.matchId),
    result = Result.valueOf(this.result),
    teamA = this.teamA.map { UserId(it) },
    teamB = this.teamB.map { UserId(it) },
)
