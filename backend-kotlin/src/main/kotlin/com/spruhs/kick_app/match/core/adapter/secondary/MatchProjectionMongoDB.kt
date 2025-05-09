package com.spruhs.kick_app.match.core.adapter.secondary

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.match.core.domain.*
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
class MatchPersistenceAdapter(private val repository: MatchRepository) : MatchProjectionPort {
    override suspend fun whenEvent(event: BaseEvent) {
        when (event) {
            is MatchPlannedEvent -> handleMatchPlannedEvent(event)
            is PlayerAddedToCadreEvent -> handlePlayerAddedToCadreEvent(event)
            is PlayerDeregisteredEvent -> handlePlayerDeregisteredEvent(event)
            is PlayerPlacedOnWaitingBenchEvent -> handlePlayerPlacedOnWaitingBenchEvent(event)
            is MatchCanceledEvent -> handleMatchCanceledEvent(event)
            is PlaygroundChangedEvent -> handlePlaygroundChangedEvent(event)
            is MatchResultEnteredEvent -> handleMatchResultEnteredEvent(event)
            else -> throw UnknownEventTypeException(event)
        }
    }

    override suspend  fun findById(matchId: MatchId): MatchProjection? =
        repository.findById(matchId.value)
            .awaitFirstOrNull()
            ?.toProjection()

    override suspend fun findAllByGroupId(groupId: GroupId): List<MatchProjection> =
        repository.findByGroupId(groupId.value).collectList()
            .awaitSingle()
            .map { it.toProjection() }

    private suspend fun handleMatchPlannedEvent(event: MatchPlannedEvent) {
        val matchDocument = MatchDocument(
            id = event.aggregateId,
            groupId = event.groupId.value,
            start = event.start,
            playground = event.playground,
            maxPlayer = event.maxPlayer,
            minPlayer = event.minPlayer,
            isCanceled = false,
            cadrePlayers = emptySet(),
            deregisteredPlayers = emptySet(),
            waitingBenchPlayers = emptySet(),
            teamA = emptySet(),
            teamB = emptySet(),
            result = null
        )
        repository.save(matchDocument).subscribe()
    }

    private suspend fun findMatch(matchId: String): MatchDocument =
        repository.findById(matchId)
            .awaitFirstOrNull()
            ?: throw MatchNotFoundException(MatchId(matchId))

    private suspend fun handlePlayerAddedToCadreEvent(event: PlayerAddedToCadreEvent) {
        val match = findMatch(event.aggregateId)
        match.cadrePlayers += event.userId.value
        match.deregisteredPlayers -= event.userId.value
        match.waitingBenchPlayers -= event.userId.value
        repository.save(match).subscribe()
    }

    private suspend fun handlePlayerDeregisteredEvent(event: PlayerDeregisteredEvent) {
        val match = findMatch(event.aggregateId)
        match.cadrePlayers -= event.userId.value
        match.deregisteredPlayers += event.userId.value
        match.waitingBenchPlayers -= event.userId.value
        repository.save(match).subscribe()
    }

    private suspend fun handlePlayerPlacedOnWaitingBenchEvent(event: PlayerPlacedOnWaitingBenchEvent) {
        val match = findMatch(event.aggregateId)
        match.cadrePlayers -= event.userId.value
        match.deregisteredPlayers -= event.userId.value
        match.waitingBenchPlayers += event.userId.value
        repository.save(match).subscribe()
    }

    private suspend fun handleMatchCanceledEvent(event: MatchCanceledEvent) {
        val match = findMatch(event.aggregateId)
        match.isCanceled = true
        repository.save(match).subscribe()
    }

    private suspend fun handlePlaygroundChangedEvent(event: PlaygroundChangedEvent) {
        val match = findMatch(event.aggregateId)
        match.playground = event.newPlayground
        repository.save(match).subscribe()
    }

    private suspend fun handleMatchResultEnteredEvent(event: MatchResultEnteredEvent) {
        val match = findMatch(event.aggregateId)
        match.result = event.result
        match.teamA = event.teamA.map { it.value }.toSet()
        match.teamB = event.teamB.map { it.value }.toSet()
        repository.save(match).subscribe()
    }
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
    playground = playground?.let { Playground(it) },
    isCanceled = this.isCanceled,
    playerCount = PlayerCount(MinPlayer(minPlayer), MaxPlayer(maxPlayer)),
    result = result?.let { Result.valueOf(it) },
    cadrePlayers = cadrePlayers.map { UserId(it) }.toSet(),
    waitingBenchPlayers = waitingBenchPlayers.map { UserId(it) }.toSet(),
    deregisteredPlayers = deregisteredPlayers.map { UserId(it) }.toSet(),
    teamA = teamA.map { UserId(it) }.toSet(),
    teamB = teamB.map { UserId(it) }.toSet(),
)