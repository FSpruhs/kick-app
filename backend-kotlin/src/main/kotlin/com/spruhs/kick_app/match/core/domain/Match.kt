package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.es.AggregateRoot
import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.es.UnknownEventTypeException
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.generateId
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlayerResult
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.match.core.application.PlanMatchCommand
import java.time.LocalDateTime

sealed class RegisteredPlayer(
    val registrationTime: LocalDateTime,
    val status: RegistrationStatus
) {
    data class MainPlayer(
        val userId: UserId,
        val guests: Int,
        val registeredAt: LocalDateTime,
        val registrationStatus: RegistrationStatus
    ) : RegisteredPlayer(registeredAt, registrationStatus)

    data class GuestPlayer(
        val guestId: String,
        val guestOf: UserId,
        val registeredAt: LocalDateTime,
        val registrationStatus: RegistrationStatus
    ) : RegisteredPlayer(registeredAt, registrationStatus)
}


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

data class MatchStartTimeException(val matchId: MatchId) :
    RuntimeException("Could not perform action with this match start time of: ${matchId.value}")

data class MatchCanceledException(val matchId: MatchId) :
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

    override fun whenEvent(event: BaseEvent) {
        when (event) {
            is MatchPlannedEvent -> handleMatchPlannedEvent(event)
            is PlayerAddedToCadreEvent -> handlePlayerStatusChange(
                event.userId,
                RegistrationStatusType.valueOf(event.status),
                cadre,
                event.guests,
                event.guestOf
            )

            is PlayerDeregisteredEvent -> handlePlayerStatusChange(
                event.userId,
                RegistrationStatusType.valueOf(event.status),
                deregistered,
                event.guests,
                event.guestOf
            )

            is PlayerPlacedOnWaitingBenchEvent -> handlePlayerStatusChange(
                event.userId,
                RegistrationStatusType.valueOf(event.status),
                waitingBench,
                event.guests,
                event.guestOf
            )

            is MatchCanceledEvent -> handleMatchCanceledEvent()
            is PlaygroundChangedEvent -> handlePlaygroundChangedEvent(event)
            is MatchResultEnteredEvent -> {}
            else -> throw UnknownEventTypeException(event)
        }
    }

    private fun handleMatchPlannedEvent(event: MatchPlannedEvent) {
        this.groupId = event.groupId
        this.start = event.start
        this.playground = event.playground?.let { Playground(it) }
        this.playerCount = PlayerCount(MinPlayer(event.minPlayer), MaxPlayer(event.maxPlayer))
    }

    private fun findPlayerRegistration(userId: UserId): RegisteredPlayer.MainPlayer? =
        (cadre + waitingBench + deregistered)
            .filterIsInstance<RegisteredPlayer.MainPlayer>()
            .find { it.userId == userId }

    private fun findGuestRegistration(userId: UserId): RegisteredPlayer.GuestPlayer? =
        (cadre + waitingBench + deregistered)
            .filterIsInstance<RegisteredPlayer.GuestPlayer>()
            .find { it.guestId == userId.value }

    private fun handlePlayerStatusChange(
        userId: UserId,
        status: RegistrationStatusType,
        targetList: MutableList<RegisteredPlayer>,
        guests: Int,
        guestOf: UserId? = null
    ) {
        if (guestOf == null) {
            val playerRegistration = findPlayerRegistration(userId)
            if (playerRegistration == null) {
                targetList.add(RegisteredPlayer.MainPlayer(userId, guests, LocalDateTime.now(), status.toRegistrationStatus()))
            } else {
                cadre.remove(playerRegistration)
                waitingBench.remove(playerRegistration)
                deregistered.remove(playerRegistration)
                targetList.add(playerRegistration.copy(registrationStatus = status.toRegistrationStatus()))
            }
        } else {
            val playerRegistration = findGuestRegistration(userId)
            if (playerRegistration == null) {
                targetList.add(RegisteredPlayer.GuestPlayer(userId.value, guestOf, LocalDateTime.now(), status.toRegistrationStatus()))
            } else {
                cadre.remove(playerRegistration)
                waitingBench.remove(playerRegistration)
                deregistered.remove(playerRegistration)
                targetList.add(playerRegistration.copy(registrationStatus = status.toRegistrationStatus()))
            }
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

    private fun validateDrawPlayers(results: Set<PlayerResult>) {
        if (results.size != 1) {
            throw IllegalArgumentException("If one player has a draw, all players must have a draw result.")
        }
    }

    private fun validatePlayers(
        results: Set<PlayerResult>,
        participatingPlayers: List<ParticipatingPlayer>,
        result: PlayerResult
    ) {
        if (PlayerResult.DRAW in results) {
            throw IllegalArgumentException("If one player has a win, no player can have a draw result.")
        }

        val winningTeams = participatingPlayers
            .filter { it.playerResult == result }
            .map { it.team }
            .toSet()

        if (winningTeams.size != 1) {
            throw IllegalArgumentException("If one player has a win, all winning players must be in the same team.")
        }
    }

    private fun validateParticipatingPlayersInput(participatingPlayers: List<ParticipatingPlayer>) {
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
            PlayerResult.DRAW in results -> validateDrawPlayers(results)
            PlayerResult.WIN in results -> validatePlayers(results, participatingPlayers, PlayerResult.WIN)
            PlayerResult.LOSS in results -> validatePlayers(results, participatingPlayers, PlayerResult.LOSS)
        }
    }

    fun enterResult(participatingPlayers: List<ParticipatingPlayer>) {
        validateParticipatingPlayersInput(participatingPlayers)

        apply(
            MatchResultEnteredEvent(
                aggregateId = aggregateId,
                groupId = groupId,
                start = start,
                players = participatingPlayers
            )
        )
    }

    private fun handleFirstRegistration(userId: UserId, registrationStatusType: RegistrationStatusType, guests: Int) {
        when (registrationStatusType) {
            RegistrationStatusType.DEREGISTERED -> handlePlayerDeregistration(
                userId,
                RegistrationStatusType.DEREGISTERED
            )

            RegistrationStatusType.REGISTERED ->
                handlePlayerRegistration(userId, RegistrationStatusType.REGISTERED, guests)

            RegistrationStatusType.CANCELLED -> return
            RegistrationStatusType.ADDED -> return
        }
    }

    private fun handleNewStatus(currentPlayer: RegisteredPlayer.MainPlayer, newStatus: RegistrationStatus, guests: Int) {
        when (newStatus) {
            is RegistrationStatus.Registered -> handlePlayerRegistration(
                currentPlayer.userId,
                RegistrationStatusType.REGISTERED,
                guests
            )

            is RegistrationStatus.Deregistered -> handlePlayerDeregistration(
                currentPlayer.userId,
                RegistrationStatusType.DEREGISTERED,
            )

            is RegistrationStatus.Cancelled -> handlePlayerCancelled(currentPlayer.userId, RegistrationStatusType.CANCELLED)
            is RegistrationStatus.Added -> handlePlayerAdded(currentPlayer.userId, RegistrationStatusType.ADDED, guests)
        }
    }

    fun addRegistration(userId: UserId, registrationStatusType: RegistrationStatusType, guests: Int = 0) {
        require(this.start.isAfter(LocalDateTime.now())) {
            throw MatchStartTimeException(MatchId(this.aggregateId))
        }
        require(guests >= 0 && guests <= playerCount.maxPlayer.value / 2) {
            "Guest must be between 0 and ${playerCount.maxPlayer.value / 2}"
        }
        val currentPlayer = findPlayerRegistration(userId)
        if (currentPlayer == null) {
            handleFirstRegistration(userId, registrationStatusType, guests)
            return
        }

        val newStatus = currentPlayer.status.updateStatus(registrationStatusType)
        if (newStatus == currentPlayer.status) {
            if (currentPlayer.guests != guests) {
                updateJustGuests(userId, guests, newStatus)
            }
            return
        }

        handleNewStatus(currentPlayer, newStatus, guests)

        if (shouldFillCadreFromWaitingBench(newStatus)) {
            fillCadreFromWaitingBench()
        }
    }

    private fun updateJustGuests(userId: UserId, guests: Int, status: RegistrationStatus) {
        if (status != RegistrationStatus.Registered) return

        val cadreGuests = cadre.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }
        val waitingBenchGuests =
            waitingBench.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }

        val totalGuests = cadreGuests.size + waitingBenchGuests.size
        if (guests < totalGuests) {
            val guestsToRemove = totalGuests - guests
            repeat(guestsToRemove) {
                if (waitingBenchGuests.isNotEmpty()) {
                    val guestToRemove = waitingBenchGuests.first()
                    apply { PlayerDeregisteredEvent(aggregateId, UserId(guestToRemove.guestId), status.getType().name, 0, userId) }
                } else if (cadreGuests.isNotEmpty()) {
                    val guestToRemove = cadreGuests.first()
                    apply { PlayerDeregisteredEvent(aggregateId, UserId(guestToRemove.guestId), status.getType().name, 0, userId) }
                }
            }
        } else if (guests > totalGuests) {
            val guestsToAdd = guests - totalGuests
            repeat(guestsToAdd) {
                if (!isCadreFull()) {
                    apply(
                        PlayerAddedToCadreEvent(
                            aggregateId,
                            UserId(generateId()),
                            RegistrationStatusType.REGISTERED.name,
                            0,
                            userId
                        )
                    )
                } else {
                    apply(
                        PlayerPlacedOnWaitingBenchEvent(
                            aggregateId,
                            UserId(generateId()),
                            RegistrationStatusType.REGISTERED.name,
                            0,
                            userId
                        )
                    )
                }
            }
        }
    }

    private fun shouldFillCadreFromWaitingBench(newStatus: RegistrationStatus): Boolean =
        newStatus.getType() in listOf(RegistrationStatusType.DEREGISTERED, RegistrationStatusType.CANCELLED) &&
                !isCadreFull() &&
                isPlayerWaiting()

    private fun fillCadreFromWaitingBench() {
        waitingBench.sortBy { it.registrationTime }
        val openCadre = playerCount.maxPlayer.value - cadre.size
        for (registration in waitingBench.filter { it.status.getType() == RegistrationStatusType.REGISTERED }
            .filterIsInstance<RegisteredPlayer.MainPlayer>()
            .take(openCadre)) {
            apply(
                PlayerAddedToCadreEvent(
                    aggregateId,
                    registration.userId,
                    registration.status.getType().name,
                    registration.guests
                )
            )
        }

        val newOpenCadre = playerCount.maxPlayer.value - cadre.size
        for (registration in waitingBench.filter { it.status.getType() == RegistrationStatusType.REGISTERED }
            .filterIsInstance<RegisteredPlayer.GuestPlayer>()
            .take(newOpenCadre)) {
            apply(
                PlayerAddedToCadreEvent(
                    aggregateId,
                    UserId(registration.guestId),
                    registration.status.getType().name,
                    0,
                    registration.guestOf
                )
            )
        }
    }

    private fun isPlayerWaiting(): Boolean =
        waitingBench.any { it.status.getType() == RegistrationStatusType.REGISTERED }

    private fun isCadreFull(): Boolean = cadre.size >= playerCount.maxPlayer.value

    private fun matchContainsGuests(): Boolean =
        cadre.any { it is RegisteredPlayer.GuestPlayer }

    private fun handlePlayerRegistration(userId: UserId, status: RegistrationStatusType, guests: Int) {
        val totalPlayers = 1 + guests
        var matchCapacity = playerCount.maxPlayer.value - cadre.size

        if (matchCapacity == 0 && matchContainsGuests()) {
            cadre.sortBy { it.registrationTime }
            val lastGuest = cadre.filterIsInstance<RegisteredPlayer.GuestPlayer>().last()
            apply(PlayerPlacedOnWaitingBenchEvent(aggregateId, UserId(lastGuest.guestId), status.name, 0, lastGuest.guestOf))
            matchCapacity += 1
        }

        val cadreSlots = totalPlayers.coerceAtMost(matchCapacity).coerceAtLeast(0)
        var benchSlots = totalPlayers - cadreSlots

        if (cadreSlots > 0) {
            apply(PlayerAddedToCadreEvent(aggregateId, userId, status.name, guests))
        } else {
            apply(PlayerPlacedOnWaitingBenchEvent(aggregateId, userId, status.name, guests))
            benchSlots -= 1
        }

        repeat(cadreSlots - 1) {
            apply(PlayerAddedToCadreEvent(aggregateId, UserId(generateId()), status.name, 0, userId))
        }
        repeat(benchSlots) {
            apply(PlayerPlacedOnWaitingBenchEvent(aggregateId, UserId(generateId()), status.name, 0,userId))
        }
    }

    private fun handlePlayerDeregistration(userId: UserId, status: RegistrationStatusType) {
        apply(PlayerDeregisteredEvent(aggregateId, userId, status.name))

        val cadreGuests = cadre.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }
        val waitingBenchGuests = waitingBench.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }

        (cadreGuests + waitingBenchGuests).forEach { guest ->
            apply(PlayerDeregisteredEvent(aggregateId, UserId(guest.guestId), status.name, 0,userId))
        }
    }

    private fun handlePlayerAdded(userId: UserId, status: RegistrationStatusType, guests: Int) {
        apply(PlayerAddedToCadreEvent(aggregateId, userId, status.name, guests))

        val cadreGuests = cadre.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }
        val waitingBenchGuests = waitingBench.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }

        (cadreGuests + waitingBenchGuests).forEach { guest ->
            apply(PlayerAddedToCadreEvent(aggregateId, UserId(guest.guestId), status.name, 0,userId))
        }
    }

    private fun handlePlayerCancelled(userId: UserId, status: RegistrationStatusType) {
        apply(PlayerPlacedOnWaitingBenchEvent(aggregateId, userId, status.name))
        val cadreGuests = cadre.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }
        val waitingBenchGuests = waitingBench.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }

        (cadreGuests + waitingBenchGuests).forEach { guest ->
            apply(PlayerPlacedOnWaitingBenchEvent(aggregateId, UserId(guest.guestId), status.name, 0,userId))
        }
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