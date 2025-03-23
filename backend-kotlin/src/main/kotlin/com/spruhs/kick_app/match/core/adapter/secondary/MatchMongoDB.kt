package com.spruhs.kick_app.match.core.adapter.secondary

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.match.core.domain.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class MatchPersistenceAdapter(private val repository: MatchRepository) : MatchPersistencePort {
    override suspend fun save(match: Match) {
        repository.save(match.toDocument()).awaitFirstOrNull()
    }

    override suspend  fun findById(matchId: MatchId): Match? =
        repository.findById(matchId.value).awaitFirstOrNull()?.toDomain()

    override suspend fun findAllByGroupId(groupId: GroupId): List<Match> {
        return repository.findByGroupId(groupId.value).collectList().awaitSingle().map { it.toDomain() }
    }

}

@Document(collection = "matches")
data class MatchDocument(
    val id: String,
    val groupId: String,
    val start: String,
    val status: String,
    val location: String,
    val maxPlayer: Int,
    val minPlayer: Int,
    val result: String?,
    val participatingPlayers: List<ParticipatingPlayerDocument>,
    val registeredPlayers: List<RegisteredPlayerDocument>
)

data class RegisteredPlayerDocument(
    val userId: String,
    val registrationTime: String,
    val status: String
)

data class ParticipatingPlayerDocument(
    val userId: String,
    val team: String
)

@Repository
interface MatchRepository : ReactiveMongoRepository<MatchDocument, String> {
    fun findByGroupId(groupId: String): Flux<MatchDocument>
}

private fun Match.toDocument() = MatchDocument(
    id = id.value,
    groupId = groupId.value,
    start = start.toISOString(),
    location = playground.value,
    status = status.name,
    maxPlayer = playerCount.maxPlayer.value,
    minPlayer = playerCount.minPlayer.value,
    registeredPlayers = registeredPlayers.map { it.toDocument() },
    participatingPlayers = participatingPlayers.map { it.toDocument() },
    result = result?.toString()
)

private fun RegisteredPlayer.toDocument() = RegisteredPlayerDocument(
    userId = userId.value,
    registrationTime = registrationTime.toISOString(),
    status = status.name
)

private fun ParticipatingPlayer.toDocument() = ParticipatingPlayerDocument(
    userId = userId.value,
    team = team.name
)

private fun MatchDocument.toDomain() = Match(
    id = MatchId(id),
    groupId = GroupId(groupId),
    start = start.toLocalDateTime(),
    playground = Playground(location),
    status = MatchStatus.valueOf(this.status),
    playerCount = PlayerCount(MinPlayer(minPlayer), MaxPlayer(maxPlayer)),
    registeredPlayers = registeredPlayers.map { it.toDomain() },
    participatingPlayers = participatingPlayers.map { it.toDomain() },
    result = result?.let { Result.valueOf(it) }
)

private fun RegisteredPlayerDocument.toDomain() = RegisteredPlayer(
    userId = UserId(userId),
    registrationTime = registrationTime.toLocalDateTime(),
    status = RegistrationStatus.valueOf(status)
)

private fun ParticipatingPlayerDocument.toDomain() = ParticipatingPlayer(
    userId = UserId(userId),
    team = Team.valueOf(team)
)