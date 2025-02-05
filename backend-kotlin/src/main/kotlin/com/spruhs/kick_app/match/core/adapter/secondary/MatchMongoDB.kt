package com.spruhs.kick_app.match.core.adapter.secondary

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.match.core.domain.*
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

@Service
class MatchPersistenceAdapter(val repository: MatchRepository) : MatchPersistencePort {
    override fun save(match: Match) {
        repository.save(match.toDocument())
    }

    override fun findById(matchId: MatchId): Match? =
        repository.findById(matchId.value).map { it.toDomain() }.orElse(null)

}

@Document(collection = "matches")
data class MatchDocument(
    val id: String,
    val groupId: String,
    val start: String,
    val location: String,
    val maxPlayer: Int,
    val minPlayer: Int,
    val registeredPlayers: List<RegisteredPlayerDocument>
)

data class RegisteredPlayerDocument(
    val userId: String,
    val registrationTime: String,
    val status: String
)

@Repository
interface MatchRepository : MongoRepository<MatchDocument, String>

private fun Match.toDocument() = MatchDocument(
    id = id.value,
    groupId = groupId.value,
    start = start.toISOString(),
    location = playground.value,
    maxPlayer = playerCount.maxPlayer.value,
    minPlayer = playerCount.minPlayer.value,
    registeredPlayers = registeredPlayers.map { it.toDocument() }
)

private fun RegisteredPlayer.toDocument() = RegisteredPlayerDocument(
    userId = userId.value,
    registrationTime = registrationTime.toISOString(),
    status = status.name
)

private fun MatchDocument.toDomain() = Match(
    id = MatchId(id),
    groupId = GroupId(groupId),
    start = start.toLocalDateTime(),
    playground = Playground(location),
    playerCount = PlayerCount(MinPlayer(minPlayer), MaxPlayer(maxPlayer)),
    registeredPlayers = registeredPlayers.map { it.toDomain() }
)

private fun RegisteredPlayerDocument.toDomain() = RegisteredPlayer(
    userId = UserId(userId),
    registrationTime = registrationTime.toLocalDateTime(),
    status = RegistrationStatus.valueOf(status)
)