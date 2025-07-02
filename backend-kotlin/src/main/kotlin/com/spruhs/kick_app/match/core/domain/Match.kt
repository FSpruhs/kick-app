package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.MatchTeam
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlayerResult
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.match.core.application.PlanMatchCommand
import java.time.LocalDateTime

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

class MatchNotFoundException(matchId: MatchId) : RuntimeException("Match not found with id: ${matchId.value}")
class MatchStartTimeException(matchId: MatchId) :
    RuntimeException("Could not perform action with this match start time of: ${matchId.value}")

class MatchCanceledException(matchId: MatchId) :
    RuntimeException("Match with id: ${matchId.value} is cancelled")

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
            is MatchResultEnteredEvent -> {println()}
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

    fun enterResult(participatingPlayers: List<ParticipatingPlayer>) {
        require(!this.isCanceled) { throw MatchCanceledException(MatchId(this.aggregateId)) }
        require(LocalDateTime.now().isAfter(this.start)) { throw MatchStartTimeException(MatchId(this.aggregateId)) }
        require(participatingPlayers.size >= 2) {
            "At least two players must participate."
        }

        require(participatingPlayers.map { it.team }.toSet().size == 2) {
            "Both teams must be present in the result."
        }

        val results = participatingPlayers.map { it.playerResult }.toSet()

        when {
            PlayerResult.DRAW in results -> {
                if (results.size != 1) {
                    throw IllegalArgumentException("If one player has a draw, all players must have a draw result.")
                }
            }

            PlayerResult.WIN in results -> {
                if (PlayerResult.DRAW in results) {
                    throw IllegalArgumentException("If one player has a win, no player can have a draw result.")
                }

                val winningTeams = participatingPlayers
                    .filter { it.playerResult == PlayerResult.WIN }
                    .map { it.team }
                    .toSet()

                if (winningTeams.size != 1) {
                    throw IllegalArgumentException("If one player has a win, all winning players must be in the same team.")
                }
            }

            PlayerResult.LOSS in results -> {
                if (PlayerResult.DRAW in results) {
                    throw IllegalArgumentException("If one player has a loss, no player can have a draw result.")
                }

                val losingTeams = participatingPlayers
                    .filter { it.playerResult == PlayerResult.LOSS }
                    .map { it.team }
                    .toSet()

                if (losingTeams.size != 1) {
                    throw IllegalArgumentException("If one player has a loss, all losing players must be in the same team.")
                }
            }
        }

        apply(
            MatchResultEnteredEvent(
                aggregateId = aggregateId,
                groupId = groupId,
                start = start,
                players = participatingPlayers
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
            val openCadre = playerCount.maxPlayer.value - cadre.size
            for (registration in waitingBench.filter { it.status.getType() == RegistrationStatusType.REGISTERED }
                .take(openCadre)) {
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