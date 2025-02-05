package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.generateId
import java.time.LocalDateTime

data class Match(
    val id: MatchId,
    val groupId: GroupId,
    val start: LocalDateTime,
    val playground: Playground,
    val playerCount: PlayerCount,
    val registeredPlayers: List<RegisteredPlayer>
)

fun planMatch(
    groupId: GroupId,
    start: LocalDateTime,
    playground: Playground,
    playerCount: PlayerCount
): Match = Match(
    id = MatchId(generateId()),
    groupId = groupId,
    start = start,
    playground = playground,
    playerCount = playerCount,
    registeredPlayers = emptyList()
)


data class RegisteredPlayer(
    val userId: UserId,
    val registrationTime: LocalDateTime,
    val status: RegistrationStatus
)

data class PlayerCount(
    val minPlayer: MinPlayer,
    val maxPlayer: MaxPlayer
) {
    init {
        require(minPlayer.value <= maxPlayer.value) { "Min player must be less or equal than max player" }
    }
}

enum class RegistrationStatus {
    REGISTERED,
    DEREGISTERED,
    CANCELLED
}

@JvmInline
value class Playground(val value: String) {
    init {
        require(value.isNotBlank()) { "Playground must not be blank" }
        require(value.length in 3..100) { "Playground must be between 3 and 100 characters" }
    }
}

@JvmInline
value class MaxPlayer(val value: Int) {
    init {
        require(value in 4..1_000) { "Max player must be between 4 and 1000" }
    }
}

@JvmInline
value class MinPlayer(val value: Int) {
    init {
        require(value in 4..1_000) { "Min player must be between 4 and 1000" }
    }
}

interface MatchPersistencePort {
    fun save(match: Match)
    fun findById(matchId: MatchId): Match?
}