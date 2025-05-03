package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.group.core.domain.GroupAggregate
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.MatchStartedEvent
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnSubstituteBenchEvent
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.match.core.application.PlanMatchCommand
import java.time.LocalDateTime

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

enum class RegistrationStatusType {
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

interface MatchProjectionPort {
    suspend fun findById(matchId: MatchId): MatchProjektion?
    suspend fun findAllByGroupId(groupId: GroupId): List<MatchProjektion>
}

data class MatchProjektion(
    val id: MatchId
)

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
    var registeredPlayers: List<RegisteredPlayer> = mutableListOf()
    var result: Result? = null
    var participatingPlayers: List<ParticipatingPlayer> = mutableListOf()

    override fun whenEvent(event: Any) {
        when (event) {
            is MatchPlannedEvent -> handleMatchPlannedEvent(event)
            is PlayerAddedToCadreEvent -> handleRegistrationEvent(
                event.userId,
                RegistrationStatusType.valueOf(event.status)
            )

            is PlayerDeregisteredEvent -> handleRegistrationEvent(
                event.userId,
                RegistrationStatusType.valueOf(event.status)
            )

            is PlayerPlacedOnSubstituteBenchEvent -> handleRegistrationEvent(
                event.userId,
                RegistrationStatusType.valueOf(event.status)
            )

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

    private fun handleRegistrationEvent(userId: UserId, registrationStatusType: RegistrationStatusType) {
        val currentPlayer = registeredPlayers.find { it.userId == userId }
        if (currentPlayer != null) {
            this.registeredPlayers -= currentPlayer
        }
        val registrationStatus = when (registrationStatusType) {
            RegistrationStatusType.REGISTERED -> RegistrationStatus.Registered
            RegistrationStatusType.DEREGISTERED -> RegistrationStatus.Deregistered
            RegistrationStatusType.CANCELLED -> RegistrationStatus.Cancelled
            RegistrationStatusType.ADDED -> RegistrationStatus.Added
        }
        this.registeredPlayers += RegisteredPlayer(userId, LocalDateTime.now(), registrationStatus)
    }

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

    fun addRegistration(userId: UserId, registrationStatusType: RegistrationStatusType) {
        require(this.start.isBefore(LocalDateTime.now())) {
            throw MatchStartTimeException(MatchId(this.aggregateId))
        }

        val currentPlayer = registeredPlayers.find { it.userId == userId }
        if (currentPlayer == null) {
            when (registrationStatusType) {
                RegistrationStatusType.DEREGISTERED -> handlePlayerDeregistration(
                    userId,
                    RegistrationStatusType.DEREGISTERED
                )

                RegistrationStatusType.REGISTERED -> handlePlayerRegistration(userId, RegistrationStatusType.REGISTERED)
                RegistrationStatusType.CANCELLED -> return
                RegistrationStatusType.ADDED -> return
            }
            return
        }

        val newStatus = currentPlayer.status.updateStatus(registrationStatusType)
        if (newStatus == currentPlayer.status) {
            return
        }

        when (newStatus) {
            is RegistrationStatus.Registered -> handlePlayerRegistration(userId, RegistrationStatusType.REGISTERED)
            is RegistrationStatus.Deregistered -> handlePlayerDeregistration(
                userId,
                RegistrationStatusType.DEREGISTERED
            )

            is RegistrationStatus.Cancelled -> handlePlayerCancelled(userId, RegistrationStatusType.CANCELLED)
            is RegistrationStatus.Added -> handlePlayerAdded(userId, RegistrationStatusType.ADDED)
        }
    }

    private fun isCadreFull(): Boolean =
        registeredPlayers.filter { it.status.getType() == RegistrationStatusType.ADDED || it.status.getType() == RegistrationStatusType.REGISTERED }.size >= playerCount.maxPlayer.value

    private fun handlePlayerRegistration(userId: UserId, status: RegistrationStatusType) {
        if (isCadreFull()) {
            apply(PlayerPlacedOnSubstituteBenchEvent(aggregateId, userId, status.name))
        } else {
            apply(PlayerAddedToCadreEvent(aggregateId, userId, status.name))
        }
    }

    private fun handlePlayerDeregistration(userId: UserId, status: RegistrationStatusType) {
        apply(PlayerDeregisteredEvent(aggregateId, userId, status.name))
    }

    private fun handlePlayerAdded(userId: UserId, status: RegistrationStatusType) {
        apply(PlayerAddedToCadreEvent(aggregateId, userId, status.name))
    }

    private fun handlePlayerCancelled(userId: UserId, status: RegistrationStatusType) {
        apply(PlayerPlacedOnSubstituteBenchEvent(aggregateId, userId, status.name))
    }

    companion object {
        const val TYPE = "Match"
    }
}

sealed class RegistrationStatus {
    abstract fun updateStatus(status: RegistrationStatusType): RegistrationStatus
    abstract fun getType(): RegistrationStatusType

    object Registered : RegistrationStatus() {
        override fun updateStatus(status: RegistrationStatusType): RegistrationStatus = when (status) {
            RegistrationStatusType.REGISTERED -> this
            RegistrationStatusType.DEREGISTERED -> Deregistered
            RegistrationStatusType.CANCELLED -> Cancelled
            RegistrationStatusType.ADDED -> this
        }

        override fun getType(): RegistrationStatusType {
            return RegistrationStatusType.REGISTERED
        }
    }

    object Deregistered : RegistrationStatus() {
        override fun updateStatus(status: RegistrationStatusType): RegistrationStatus = when (status) {
            RegistrationStatusType.REGISTERED -> Registered
            RegistrationStatusType.DEREGISTERED -> this
            RegistrationStatusType.CANCELLED -> this
            RegistrationStatusType.ADDED -> this
        }

        override fun getType(): RegistrationStatusType {
            return RegistrationStatusType.DEREGISTERED
        }
    }

    object Cancelled : RegistrationStatus() {
        override fun updateStatus(status: RegistrationStatusType): RegistrationStatus = when (status) {
            RegistrationStatusType.REGISTERED -> this
            RegistrationStatusType.DEREGISTERED -> this
            RegistrationStatusType.CANCELLED -> this
            RegistrationStatusType.ADDED -> Added
        }

        override fun getType(): RegistrationStatusType {
            return RegistrationStatusType.CANCELLED
        }
    }

    object Added : RegistrationStatus() {
        override fun updateStatus(status: RegistrationStatusType): RegistrationStatus = when (status) {
            RegistrationStatusType.REGISTERED -> this
            RegistrationStatusType.DEREGISTERED -> Deregistered
            RegistrationStatusType.CANCELLED -> Cancelled
            RegistrationStatusType.ADDED -> this
        }

        override fun getType(): RegistrationStatusType {
            return RegistrationStatusType.ADDED
        }
    }
}