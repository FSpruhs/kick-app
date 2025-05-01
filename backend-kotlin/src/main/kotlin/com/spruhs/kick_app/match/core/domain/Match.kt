package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.group.core.domain.GroupAggregate
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchCreatedEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.MatchStartedEvent
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnSubstituteBenchEvent
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.match.api.ResultAddedEvent
import com.spruhs.kick_app.match.core.application.PlanMatchCommand
import java.time.LocalDateTime

data class Match(
    val id: MatchId,
    val groupId: GroupId,
    val start: LocalDateTime,
    val status: MatchStatus,
    val playground: Playground,
    val playerCount: PlayerCount,
    val registeredPlayers: List<RegisteredPlayer>,
    val result: Result? = null,
    val participatingPlayers: List<ParticipatingPlayer> = emptyList(),
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
        domainEvents = listOf(MatchCreatedEvent(groupId.value, newId, start))
    )
}

private fun Match.findRegisteredPlayer(userId: UserId): RegisteredPlayer? {
    return this.registeredPlayers.find { it.userId == userId }
}

fun Match.cancel(): Match {
    require(this.status == MatchStatus.PLANNED) { "Cannot cancel match with status: ${this.status}" }
    return this.copy(status = MatchStatus.CANCELLED)
}

fun Match.addRegistration(userId: UserId, registrationStatus: RegistrationStatus): Match {
    require(
        registrationStatus == RegistrationStatus.REGISTERED ||
                registrationStatus == RegistrationStatus.DEREGISTERED
    ) { "Can only register or deregister" }
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
        registeredPlayers = this.registeredPlayers - player + player.copy(
            status = registrationStatus,
            registrationTime = LocalDateTime.now()
        )
    )
}

fun Match.updateRegistration(updatedUser: UserId, registrationStatus: RegistrationStatus): Match {
    require(
        registrationStatus == RegistrationStatus.CANCELLED ||
                registrationStatus == RegistrationStatus.ADDED
    ) { "Can only cancel or add" }
    require(this.start.isBefore(LocalDateTime.now())) { "Cannot register to past match" }
    val player = this.findRegisteredPlayer(updatedUser).takeIf { it?.status != RegistrationStatus.DEREGISTERED }
        ?: throw IllegalStateException("Player not found")
    return this.copy(
        registeredPlayers = this.registeredPlayers - player + player.copy(
            status = registrationStatus,
        )
    )
}

fun Match.addResult(result: Result, teamA: Set<UserId>, teamB: Set<UserId>): Match {
    require(this.status != MatchStatus.CANCELLED) { "Cannot add result to cancelled match" }
    require(this.start.isBefore(LocalDateTime.now())) { "Cannot add result to future match" }
    require(teamA.none { teamB.contains(it) }) { "Players cannot be in both teams" }

    return this.copy(
        status = MatchStatus.FINISHED,
        result = result,
        participatingPlayers = teamA.map { ParticipatingPlayer(it, Team.A) } + teamB.map {
            ParticipatingPlayer(
                it,
                Team.B
            )
        },
        domainEvents = this.domainEvents + ResultAddedEvent(
            this.id.value,
            result.toString(),
            teamA.map { it.value },
            teamB.map { it.value })
    )
}

fun Match.acceptedPlayers(): List<UserId> {
    val registeredPlayer = this.registeredPlayers
        .filter { it.status == RegistrationStatus.REGISTERED }
        .sortedBy { it.registrationTime }
        .take(this.playerCount.maxPlayer.value)

    val addedPlayers = this.registeredPlayers.filter { it.status == RegistrationStatus.ADDED }

    return (registeredPlayer + addedPlayers).map { it.userId }
}

fun Match.waitingBenchPlayers(): List<UserId> {
    val benchPlayers = this.registeredPlayers
        .filter { it.status == RegistrationStatus.DEREGISTERED }
        .sortedBy { it.registrationTime }
        .drop(this.playerCount.maxPlayer.value)

    val canceledPlayers = this.registeredPlayers.filter { it.status == RegistrationStatus.CANCELLED }

    return (benchPlayers + canceledPlayers).map { it.userId }
}

enum class MatchStatus {
    PLANNED,
    CANCELLED,
    ENTER_RESULT,
    FINISHED
}

enum class Result {
    WINNER_TEAM_A,
    WINNER_TEAM_B,
    DRAW
}

data class ParticipatingPlayer(
    val userId: UserId,
    val team: Team
)

enum class Team {
    A,
    B
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
    CANCELLED,
    ADDED
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
    suspend fun save(match: Match)
    suspend fun findById(matchId: MatchId): Match?
    suspend fun findAllByGroupId(groupId: GroupId): List<Match>
}

class MatchNotFoundException(matchId: MatchId) : RuntimeException("Match not found with id: ${matchId.value}")
class MatchStartTimeException(matchId: MatchId) :
    RuntimeException("Could not perform action with this match start time of: ${matchId.value}")

class MatchCanceledException(matchId: MatchId) :
    RuntimeException("Match with id: ${matchId.value} is cancelled")

class PlayerResultEnteredMultipleTimesException(
    matchId: MatchId,
) : RuntimeException("Player  entered result multiple times for match: ${matchId.value}")

class MatchAggregate(
    override val aggregateId: String,
) : AggregateRoot(aggregateId, GroupAggregate.Companion.TYPE) {

    var groupId: GroupId = GroupId("default")
    var start: LocalDateTime = LocalDateTime.now()
    var status: MatchStatus = MatchStatus.PLANNED
    var playground: Playground? = null
    var playerCount: PlayerCount = PlayerCount(MinPlayer(4), MaxPlayer(8))
    val registeredPlayers: List<RegisteredPlayer> = mutableListOf()
    var result: Result? = null
    var participatingPlayers: List<ParticipatingPlayer> = mutableListOf()

    override fun whenEvent(event: Any) {
        when (event) {
            is MatchPlannedEvent -> handleMatchPlannedEvent(event)
            is PlayerAddedToCadreEvent -> handlePlayerAddedToCadreEvent(event)
            is PlayerDeregisteredEvent -> handlePlayerDeregisteredEvent(event)
            is PlayerPlacedOnSubstituteBenchEvent -> handlePlayerPlacedOnSubstituteBenchEvent(event)
            is MatchCanceledEvent -> handleMatchCanceledEvent()
            is PlaygroundChangedEvent -> handlePlaygroundChangedEvent(event)
            is MatchResultEnteredEvent -> handleMatchResultEnteredEvent(event)
            is MatchStartedEvent -> handleMatchStartedEvent()
            else -> throw UnknownEventTypeException(event)
        }
    }

    private fun handleMatchPlannedEvent(event: MatchPlannedEvent) {
        this.groupId = event.groupId
        this.start = event.start
        this.playground = Playground(event.playground)
        this.playerCount = PlayerCount(MinPlayer(event.minPlayer), MaxPlayer(event.maxPlayer))
    }

    private fun handlePlayerAddedToCadreEvent(event: PlayerAddedToCadreEvent) {}

    private fun handlePlayerDeregisteredEvent(event: PlayerDeregisteredEvent) {}

    private fun handlePlayerPlacedOnSubstituteBenchEvent(event: PlayerPlacedOnSubstituteBenchEvent) {}

    private fun handleMatchCanceledEvent() {
        this.status = MatchStatus.CANCELLED
    }

    private fun handlePlaygroundChangedEvent(event: PlaygroundChangedEvent) {
        this.playground = Playground(event.newPlayground)
    }

    private fun handleMatchResultEnteredEvent(event: MatchResultEnteredEvent) {
        this.result = Result.valueOf(event.result)
        this.participatingPlayers =
            event.teamA.map { ParticipatingPlayer(it, Team.A) } + event.teamB.map { ParticipatingPlayer(it, Team.B) }
    }

    private fun handleMatchStartedEvent() {
        this.status = MatchStatus.ENTER_RESULT
    }

    fun planMatch(command: PlanMatchCommand) {
        apply(
            MatchPlannedEvent(
                aggregateId,
                command.groupId,
                command.start,
                command.playground.value,
                command.playerCount.maxPlayer.value,
                command.playerCount.minPlayer.value
            )
        )
    }

    fun cancelMatch() {
        require(LocalDateTime.now().isBefore(this.start)) { throw MatchStartTimeException(MatchId(this.aggregateId)) }
        apply(MatchCanceledEvent(aggregateId))
    }

    fun startMatch() {
        require(LocalDateTime.now().isAfter(this.start)) { throw MatchStartTimeException(MatchId(this.aggregateId)) }
        apply(MatchStartedEvent(aggregateId))
    }

    fun changePlayground(newPlayground: Playground) {
        apply(PlaygroundChangedEvent(aggregateId, newPlayground.value))
    }

    fun enterResult(result: Result, participatingPlayer: List<ParticipatingPlayer>) {
        require(this.status != MatchStatus.CANCELLED) { throw MatchCanceledException(MatchId(this.aggregateId)) }
        require(LocalDateTime.now().isAfter(this.start)) { throw MatchStartTimeException(MatchId(this.aggregateId)) }
        require(participatingPlayer.size == participatingPlayer.map { it.userId }
            .toSet().size) { throw PlayerResultEnteredMultipleTimesException(MatchId(this.aggregateId)) }

        apply(
            MatchResultEnteredEvent(
                aggregateId,
            result.name,
            participatingPlayer.filter { it.team == Team.A }.map { it.userId },
            participatingPlayer.filter { it.team == Team.B }.map { it.userId }
        ))
    }


    companion object {
        const val TYPE = "Match"
    }
}