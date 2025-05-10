package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.match.core.application.PlanMatchCommand
import java.time.LocalDateTime

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
    ADDED;

    fun toRegistrationStatus(): RegistrationStatus = when (this) {
        REGISTERED -> RegistrationStatus.Registered
        DEREGISTERED -> RegistrationStatus.Deregistered
        CANCELLED -> RegistrationStatus.Cancelled
        ADDED -> RegistrationStatus.Added
    }
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
    suspend fun whenEvent(event: BaseEvent)
    suspend fun findById(matchId: MatchId): MatchProjection?
    suspend fun findAllByGroupId(groupId: GroupId): List<MatchProjection>
}

data class MatchProjection(
    val id: MatchId,
    val groupId: GroupId,
    val start: LocalDateTime,
    val playground: Playground?,
    val isCanceled: Boolean,
    val playerCount: PlayerCount,
    val cadrePlayers: Set<UserId>,
    val waitingBenchPlayers: Set<UserId>,
    val deregisteredPlayers: Set<UserId>,
    val teamA: Set<UserId>,
    val teamB: Set<UserId>,
    val result: Result? = null,
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
) : AggregateRoot(aggregateId, TYPE) {

    var groupId: GroupId = GroupId("default")
    var start: LocalDateTime = LocalDateTime.now()
    var isCanceled: Boolean = false
    var playground: Playground? = null
    var playerCount: PlayerCount = PlayerCount(MinPlayer(4), MaxPlayer(8))
    val cadre = mutableListOf<RegisteredPlayer>()
    val waitingBench = mutableListOf<RegisteredPlayer>()
    val deregistered = mutableListOf<RegisteredPlayer>()
    var result: Result? = null
    var participatingPlayers: List<ParticipatingPlayer> = mutableListOf()

    override fun whenEvent(event: Any) {
        when (event) {
            is MatchPlannedEvent -> handleMatchPlannedEvent(event)
            is PlayerAddedToCadreEvent -> handlePlayerStatusChange(
                event.userId,
                RegistrationStatusType.valueOf(event.status),
                cadre
            )

            is PlayerDeregisteredEvent -> handlePlayerStatusChange(
                event.userId,
                RegistrationStatusType.valueOf(event.status),
                deregistered
            )

            is PlayerPlacedOnWaitingBenchEvent -> handlePlayerStatusChange(
                event.userId,
                RegistrationStatusType.valueOf(event.status),
                waitingBench
            )

            is MatchCanceledEvent -> handleMatchCanceledEvent()
            is PlaygroundChangedEvent -> handlePlaygroundChangedEvent(event)
            is MatchResultEnteredEvent -> handleMatchResultEnteredEvent(event)
            else -> throw UnknownEventTypeException(event)
        }
    }

    private fun handleMatchPlannedEvent(event: MatchPlannedEvent) {
        this.groupId = event.groupId
        this.start = event.start
        this.playground = event.playground?.let { Playground(it) }
        this.playerCount = PlayerCount(MinPlayer(event.minPlayer), MaxPlayer(event.maxPlayer))
    }

    private fun findPlayerRegistration(userId: UserId): RegisteredPlayer? =
        (cadre + waitingBench + deregistered).find { it.userId == userId }


    private fun handlePlayerStatusChange(
        userId: UserId,
        status: RegistrationStatusType,
        targetList: MutableList<RegisteredPlayer>
    ) {
        val playerRegistration = findPlayerRegistration(userId)
        if (playerRegistration == null) {
            targetList.add(RegisteredPlayer(userId, LocalDateTime.now(), status.toRegistrationStatus()))
        } else {
            cadre.remove(playerRegistration)
            waitingBench.remove(playerRegistration)
            deregistered.remove(playerRegistration)
            targetList.add(playerRegistration.copy(status = status.toRegistrationStatus()))
        }
    }

    private fun handleMatchCanceledEvent() {
        this.isCanceled = true
    }

    private fun handlePlaygroundChangedEvent(event: PlaygroundChangedEvent) {
        this.playground = Playground(event.newPlayground)
    }

    private fun handleMatchResultEnteredEvent(event: MatchResultEnteredEvent) {
        this.result = Result.valueOf(event.result)
        this.participatingPlayers =
            event.teamA.map { ParticipatingPlayer(it, Team.A) } + event.teamB.map { ParticipatingPlayer(it, Team.B) }
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
        apply(MatchCanceledEvent(aggregateId, this.groupId))
    }

    fun changePlayground(newPlayground: Playground) {
        apply(PlaygroundChangedEvent(aggregateId, newPlayground.value, this.groupId))
    }

    fun enterResult(result: Result, participatingPlayer: List<ParticipatingPlayer>) {
        require(!this.isCanceled) { throw MatchCanceledException(MatchId(this.aggregateId)) }
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

        val currentPlayer = findPlayerRegistration(userId)
        if (currentPlayer == null) {
            when (registrationStatusType) {
                RegistrationStatusType.DEREGISTERED -> handlePlayerDeregistration(
                    userId,
                    RegistrationStatusType.DEREGISTERED
                )

                RegistrationStatusType.REGISTERED ->
                    handlePlayerRegistration(userId, RegistrationStatusType.REGISTERED)

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

        if ((newStatus.getType() == RegistrationStatusType.DEREGISTERED || newStatus.getType() == RegistrationStatusType.CANCELLED) && !isCadreFull() && isPlayerWaiting()) {
            waitingBench.sortBy { it.registrationTime }
            val test = playerCount.maxPlayer.value - cadre.size
            for (registration in waitingBench.take(test)) {
                if (registration.status.getType() == RegistrationStatusType.REGISTERED) {
                    apply(
                        PlayerAddedToCadreEvent(
                            aggregateId,
                            registration.userId,
                            registration.status.getType().name
                        )
                    )
                }
            }
        }
    }

    private fun isPlayerWaiting(): Boolean =
        waitingBench.any { it.status.getType() == RegistrationStatusType.REGISTERED }

    private fun isCadreFull(): Boolean = cadre.size >= playerCount.maxPlayer.value

    private fun handlePlayerRegistration(userId: UserId, status: RegistrationStatusType) {
        if (isCadreFull()) {
            apply(PlayerPlacedOnWaitingBenchEvent(aggregateId, userId, status.name))
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
        apply(PlayerPlacedOnWaitingBenchEvent(aggregateId, userId, status.name))
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