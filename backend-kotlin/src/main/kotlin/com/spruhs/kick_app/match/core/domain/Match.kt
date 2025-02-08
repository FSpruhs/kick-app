package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.match.api.MatchCreatedEvent
import java.time.LocalDateTime

data class Match(
    val id: MatchId,
    val groupId: GroupId,
    val start: LocalDateTime,
    val status: MatchStatus,
    val playground: Playground,
    val playerCount: PlayerCount,
    val registeredPlayers: List<RegisteredPlayer>,
    override val domainEvents: List<DomainEvent> = listOf()
) : DomainEventList

fun planMatch(
    groupId: GroupId,
    start: LocalDateTime,
    playground: Playground,
    playerCount: PlayerCount
): Match {
    require(start.isAfter(LocalDateTime.now())) { "Match start must be in the future" }
    val newId = generateId()
    return Match(
        id = MatchId(newId),
        groupId = groupId,
        start = start,
        playground = playground,
        status = MatchStatus.PLANNED,
        playerCount = playerCount,
        registeredPlayers = emptyList(),
        domainEvents = listOf(MatchCreatedEvent(groupId.value, newId,start))
    )
}

private fun Match.findRegisteredPlayer(userId: UserId): RegisteredPlayer? {
    return this.registeredPlayers.find { it.userId == userId }
}

fun Match.cancel(): Match {
    require(this.status == MatchStatus.PLANNED) { "Cannot cancel match with status: ${this.status}" }
    return this.copy(status = MatchStatus.CANCELLED)
}

fun Match.cancelPlayer(userId: UserId): Match {
    require(this.status == MatchStatus.PLANNED) { "Cannot cancel player in match with status: ${this.status}" }
    val player = this.findRegisteredPlayer(userId)?.takeIf { it.status == RegistrationStatus.REGISTERED }
        ?: throw IllegalArgumentException("Cannot cancel player")
    return this.copy(registeredPlayers = this.registeredPlayers - player + player.copy(status = RegistrationStatus.CANCELLED))
}

fun Match.addRegistration(userId: UserId, registrationStatus: RegistrationStatus): Match {
    require(registrationStatus != RegistrationStatus.CANCELLED) { "Cannot register with cancelled status" }
    require(this.start.isBefore(LocalDateTime.now())) { "Cannot register to past match" }
    val player = this.findRegisteredPlayer(userId)
        ?: return this.copy(
            registeredPlayers = this.registeredPlayers + RegisteredPlayer(
                userId = userId,
                registrationTime = LocalDateTime.now(),
                status = registrationStatus
            )
        )
    return this.copy(
        registeredPlayers = this.registeredPlayers - player + player.copy(status = registrationStatus)
    )
}

enum class MatchStatus {
    PLANNED,
    CANCELLED,
    FINISHED
}

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

class MatchNotFoundException(matchId: MatchId) : RuntimeException("Match not found with id: ${matchId.value}")