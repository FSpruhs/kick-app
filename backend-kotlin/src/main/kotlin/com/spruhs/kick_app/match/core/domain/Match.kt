package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.es.AggregateRoot
import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.es.UnknownEventTypeException
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchNumber
import com.spruhs.kick_app.match.api.MatchNumberChangedEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.MatchResultUpdatedEvent
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerOverviewEntry
import com.spruhs.kick_app.match.api.PlayerOverviewUpdatedEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlayerPriorityStrategyType
import com.spruhs.kick_app.match.api.PlayerResult
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import java.time.LocalDateTime

sealed class RegisteredPlayer(
    val registrationTime: LocalDateTime,
    val status: RegistrationStatus,
    val attendancePoints: Int,
) {
    data class MainPlayer(
        val userId: UserId,
        val guests: Int,
        val registeredAt: LocalDateTime,
        val registrationStatus: RegistrationStatus,
        val points: Int,
        val lastWaitingBenchMatchNumber: MatchNumber? = null,
    ) : RegisteredPlayer(registeredAt, registrationStatus, points)

    data class GuestPlayer(
        val guestId: String,
        val guestOf: UserId,
        val registeredAt: LocalDateTime,
        val registrationStatus: RegistrationStatus,
        val points: Int,
    ) : RegisteredPlayer(registeredAt, registrationStatus, points)
}

data class PlayerCount(
    val minPlayer: MinPlayer,
    val maxPlayer: MaxPlayer,
) {
    init {
        require(minPlayer.value <= maxPlayer.value) { "Min player must be less or equal than max player" }
    }
}

enum class RegistrationStatusType {
    REGISTERED,
    DEREGISTERED,
    CANCELLED,
    ADDED,
    ;

    fun toRegistrationStatus(): RegistrationStatus =
        when (this) {
            REGISTERED -> RegistrationStatus.Registered
            DEREGISTERED -> RegistrationStatus.Deregistered
            CANCELLED -> RegistrationStatus.Cancelled
            ADDED -> RegistrationStatus.Added
        }
}

@JvmInline
value class Playground(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Playground must not be blank" }
        require(value.length in 3..100) { "Playground must be between 3 and 100 characters" }
    }
}

@JvmInline
value class MaxPlayer(
    val value: Int,
) {
    init {
        require(value in 4..1_000) { "Max player must be between 4 and 1000" }
    }
}

@JvmInline
value class MinPlayer(
    val value: Int,
) {
    init {
        require(value in 4..1_000) { "Min player must be between 4 and 1000" }
    }
}

data class MatchStartTimeException(
    val matchId: MatchId,
) : RuntimeException("Could not perform action with this match start time of: ${matchId.value}")

data class MatchCanceledException(
    val matchId: MatchId,
) : RuntimeException("Match with id: ${matchId.value} is cancelled")

class MatchAggregate(
    override val aggregateId: String,
) : AggregateRoot(aggregateId, TYPE) {
    var groupId: GroupId = GroupId("default")
    var start: LocalDateTime = LocalDateTime.now()
    var matchNumber: MatchNumber = MatchNumber(0)
    var isCanceled: Boolean = false
    var playground: Playground? = null
    var playerCount: PlayerCount = PlayerCount(MinPlayer(4), MaxPlayer(8))
    val cadre = mutableListOf<RegisteredPlayer>()
    val waitingBench = mutableListOf<RegisteredPlayer>()
    val deregistered = mutableListOf<RegisteredPlayer>()
    var playerPriorityStrategy: PlayerPriorityStrategy = FirstComeFirstServe()
    var result: List<ParticipatingPlayer> = emptyList()

    override fun whenEvent(event: BaseEvent) {
        when (event) {
            is MatchPlannedEvent -> handleMatchPlannedEvent(event)
            is PlayerAddedToCadreEvent ->
                handlePlayerStatusChange(
                    event.userId,
                    RegistrationStatusType.valueOf(event.status),
                    cadre,
                    event.guests,
                    event.guestOf,
                    event.attendancePoints,
                    event.lastWaitingBenchMatchNumber,
                )

            is PlayerDeregisteredEvent ->
                handlePlayerStatusChange(
                    event.userId,
                    RegistrationStatusType.valueOf(event.status),
                    deregistered,
                    event.guests,
                    event.guestOf,
                    0,
                )

            is PlayerPlacedOnWaitingBenchEvent ->
                handlePlayerStatusChange(
                    event.userId,
                    RegistrationStatusType.valueOf(event.status),
                    waitingBench,
                    event.guests,
                    event.guestOf,
                    event.attendancePoints,
                    event.lastWaitingBenchMatchNumber,
                )

            is MatchCanceledEvent -> handleMatchCanceledEvent()
            is PlaygroundChangedEvent -> handlePlaygroundChanged(event)
            is MatchResultEnteredEvent -> handleMatchResultEntered(event)
            is MatchResultUpdatedEvent -> handleMatchResultUpdated(event)
            is PlayerOverviewUpdatedEvent -> handlePlayerOverviewUpdated(event)
            is MatchNumberChangedEvent -> handleMatchNumberChanged(event)
            else -> throw UnknownEventTypeException(event)
        }
    }

    private fun handleMatchNumberChanged(event: MatchNumberChangedEvent) {
        this.matchNumber = event.newMatchNumber
    }

    private fun handlePlayerOverviewUpdated(event: PlayerOverviewUpdatedEvent) {
        if (playerPriorityStrategy.type() == PlayerPriorityStrategyType.FIRST_COME_FIRST_SERVE) return

        event.players.forEach { entry ->
            val cadreIndex = cadre.indexOfFirst { it is RegisteredPlayer.MainPlayer && it.userId == entry.userId }
            if (cadreIndex >= 0) {
                val existing = cadre[cadreIndex] as RegisteredPlayer.MainPlayer
                cadre[cadreIndex] =
                    existing.copy(
                        points = entry.attendancePoints,
                        lastWaitingBenchMatchNumber = entry.lastWaitingBenchMatchNumber,
                    )
            } else {
                val benchIndex = waitingBench.indexOfFirst { it is RegisteredPlayer.MainPlayer && it.userId == entry.userId }
                if (benchIndex >= 0) {
                    val existing = waitingBench[benchIndex] as RegisteredPlayer.MainPlayer
                    waitingBench[benchIndex] =
                        existing.copy(
                            points = entry.attendancePoints,
                            lastWaitingBenchMatchNumber = entry.lastWaitingBenchMatchNumber,
                        )
                }
            }

            cadre.indices.forEach { i ->
                val guest = cadre[i]
                if (guest is RegisteredPlayer.GuestPlayer && guest.guestOf == entry.userId) {
                    cadre[i] = guest.copy(points = entry.attendancePoints)
                }
            }
            waitingBench.indices.forEach { i ->
                val guest = waitingBench[i]
                if (guest is RegisteredPlayer.GuestPlayer && guest.guestOf == entry.userId) {
                    waitingBench[i] = guest.copy(points = entry.attendancePoints)
                }
            }
        }

        playerPriorityStrategy.reevaluateRegistration(this) { apply(it) }
    }

    private fun handleMatchResultEntered(event: MatchResultEnteredEvent) {
        this.result = event.players
    }

    private fun handleMatchResultUpdated(event: MatchResultUpdatedEvent) {
        val updatedList = this.result.toMutableList()
        if (event.newTeam != null && event.newResult != null) {
            val index = updatedList.indexOfFirst { it.userId == event.user }
            val updated = ParticipatingPlayer(event.user, event.newResult, event.newTeam)
            if (index >= 0) {
                updatedList[index] = updated
            } else {
                updatedList.add(updated)
            }
        } else {
            updatedList.removeIf { it.userId == event.user }
        }
        this.result = updatedList
    }

    private fun handleMatchPlannedEvent(event: MatchPlannedEvent) {
        this.groupId = event.groupId
        this.start = event.start
        this.playground = event.playground?.let { Playground(it) }
        this.playerCount = PlayerCount(MinPlayer(event.minPlayer), MaxPlayer(event.maxPlayer))
        this.matchNumber = MatchNumber(event.matchNumber)
        this.playerPriorityStrategy = addPlayerPriorityStrategyType(event.playerPriorityStrategyType)
    }

    private fun addPlayerPriorityStrategyType(playerPriorityStrategyType: PlayerPriorityStrategyType?): PlayerPriorityStrategy =
        when (playerPriorityStrategyType) {
            PlayerPriorityStrategyType.FIRST_COME_FIRST_SERVE -> FirstComeFirstServe()
            PlayerPriorityStrategyType.ROUND_ROBIN -> RoundRobin()
            PlayerPriorityStrategyType.ATTENDANCE_BASED -> AttendanceBased()
            null -> FirstComeFirstServe()
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
        guestOf: UserId? = null,
        attendancePoints: Int = 0,
        lastWaitingBenchMatchNumber: MatchNumber? = null,
    ) {
        if (guestOf == null) {
            val playerRegistration = findPlayerRegistration(userId)
            if (playerRegistration == null) {
                targetList.add(
                    RegisteredPlayer.MainPlayer(
                        userId,
                        guests,
                        LocalDateTime.now(),
                        status.toRegistrationStatus(),
                        attendancePoints,
                        lastWaitingBenchMatchNumber,
                    ),
                )
            } else {
                cadre.remove(playerRegistration)
                waitingBench.remove(playerRegistration)
                deregistered.remove(playerRegistration)
                targetList.add(playerRegistration.copy(registrationStatus = status.toRegistrationStatus()))
            }
        } else {
            val playerRegistration = findGuestRegistration(userId)
            if (playerRegistration == null) {
                targetList.add(
                    RegisteredPlayer.GuestPlayer(userId.value, guestOf, LocalDateTime.now(), status.toRegistrationStatus(), attendancePoints),
                )
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

    private fun handlePlaygroundChanged(event: PlaygroundChangedEvent) {
        this.playground = Playground(event.newPlayground)
    }

    fun planMatch(
        groupId: GroupId,
        start: LocalDateTime,
        playground: Playground?,
        playerCount: PlayerCount,
        lastMatchNumber: MatchNumber,
        playerPriorityStrategyType: PlayerPriorityStrategyType? = null,
    ) {
        apply(
            MatchPlannedEvent(
                aggregateId,
                groupId,
                start,
                playground?.value,
                playerCount.maxPlayer.value,
                playerCount.minPlayer.value,
                lastMatchNumber.value + 1,
                playerPriorityStrategyType,
            ),
        )
    }

    fun updatePlayerOverview(overview: PlayerOverview) {
        if (start.isBefore(LocalDateTime.now())) return

        apply(PlayerOverviewUpdatedEvent(aggregateId, overview.entries))
    }

    fun cancelMatch() {
        require(LocalDateTime.now().isBefore(this.start)) { throw MatchStartTimeException(MatchId(this.aggregateId)) }
        apply(MatchCanceledEvent(aggregateId, this.groupId))
    }

    fun changePlayground(newPlayground: Playground) {
        apply(PlaygroundChangedEvent(aggregateId, newPlayground.value, this.groupId))
    }

    private fun validateDrawPlayers(results: Set<PlayerResult>) {
        require(results.size == 1) {
            throw IllegalArgumentException("If one player has a draw, all players must have a draw result.")
        }
    }

    private fun validatePlayers(
        results: Set<PlayerResult>,
        participatingPlayers: List<ParticipatingPlayer>,
        result: PlayerResult,
    ) {
        require(PlayerResult.DRAW !in results) {
            throw IllegalArgumentException("If one player has a win, no player can have a draw result.")
        }

        val winningTeams =
            participatingPlayers
                .filter { it.playerResult == result }
                .map { it.team }
                .toSet()

        require(winningTeams.size == 1) {
            throw IllegalArgumentException("If one player has a win, all winning players must be in the same team.")
        }
    }

    private fun validateParticipatingPlayersInput(participatingPlayers: List<ParticipatingPlayer>) {
        require(!this.isCanceled) { throw MatchCanceledException(MatchId(this.aggregateId)) }
        require(LocalDateTime.now().isAfter(this.start)) { throw MatchStartTimeException(MatchId(this.aggregateId)) }
        require(arePlayersUnique(participatingPlayers))
        require(participatingPlayers.size >= 2) { "At least two players must participate." }
        require(participatingPlayers.map { it.team }.toSet().size == 2) { "Both teams must be present in the result." }

        val results = participatingPlayers.map { it.playerResult }.toSet()
        when {
            PlayerResult.DRAW in results -> validateDrawPlayers(results)
            PlayerResult.WIN in results -> validatePlayers(results, participatingPlayers, PlayerResult.WIN)
            PlayerResult.LOSS in results -> validatePlayers(results, participatingPlayers, PlayerResult.LOSS)
        }
    }

    fun enterResult(participatingPlayers: List<ParticipatingPlayer>): EnterResultResponse {
        validateParticipatingPlayersInput(participatingPlayers)

        return if (this.result.isEmpty()) {
            apply(
                MatchResultEnteredEvent(
                    aggregateId = aggregateId,
                    groupId = groupId,
                    start = start,
                    players = participatingPlayers,
                ),
            )

            EnterResultResponse.FirstEntry
        } else {
            updateResult(participatingPlayers)
            EnterResultResponse.ResultUpdated
        }
    }

    private fun updateResult(participatingPlayers: List<ParticipatingPlayer>) {
        participatingPlayers.forEach { participatingPlayer ->
            val player = result.find { it.userId == participatingPlayer.userId }
            if (player == null) {
                apply(
                    MatchResultUpdatedEvent(
                        aggregateId = aggregateId,
                        groupId = this.groupId,
                        user = participatingPlayer.userId,
                        matchNumber = this.matchNumber,
                        oldTeam = null,
                        oldResult = null,
                        newTeam = participatingPlayer.team,
                        newResult = participatingPlayer.playerResult,
                    ),
                )
            } else if (player.team != participatingPlayer.team || player.playerResult != participatingPlayer.playerResult) {
                apply(
                    MatchResultUpdatedEvent(
                        aggregateId = aggregateId,
                        groupId = this.groupId,
                        user = participatingPlayer.userId,
                        matchNumber = this.matchNumber,
                        oldTeam = player.team,
                        oldResult = player.playerResult,
                        newTeam = participatingPlayer.team,
                        newResult = participatingPlayer.playerResult,
                    ),
                )
            }
        }

        this.result.forEach { participatingPlayer ->
            val player = participatingPlayers.find { it.userId == participatingPlayer.userId }
            if (player == null) {
                apply(
                    MatchResultUpdatedEvent(
                        aggregateId = aggregateId,
                        groupId = this.groupId,
                        user = participatingPlayer.userId,
                        matchNumber = this.matchNumber,
                        oldTeam = participatingPlayer.team,
                        oldResult = participatingPlayer.playerResult,
                        newTeam = null,
                        newResult = null,
                    ),
                )
            }
        }
    }

    private fun arePlayersUnique(participatingPlayers: List<ParticipatingPlayer>): Boolean {
        val result =
            participatingPlayers
                .groupBy {
                    it.userId
                }.values
                .find { it.size > 1 }
        return result == null
    }

    fun addRegistration(
        userId: UserId,
        registrationStatusType: RegistrationStatusType,
        guests: Int = 0,
        playerOverview: PlayerOverviewEntry? = null,
    ) {
        playerPriorityStrategy.addRegistration(userId, registrationStatusType, guests, playerOverview, this) { apply(it) }
    }

    fun cadreCapacity() = playerCount.maxPlayer.value - cadre.size

    companion object {
        const val TYPE = "Match"
    }
}

sealed interface EnterResultResponse {
    object FirstEntry : EnterResultResponse

    object ResultUpdated : EnterResultResponse
}

sealed class RegistrationStatus {
    abstract fun updateStatus(status: RegistrationStatusType): RegistrationStatus

    abstract fun getType(): RegistrationStatusType

    object Registered : RegistrationStatus() {
        override fun updateStatus(status: RegistrationStatusType): RegistrationStatus =
            when (status) {
                RegistrationStatusType.REGISTERED -> this
                RegistrationStatusType.DEREGISTERED -> Deregistered
                RegistrationStatusType.CANCELLED -> Cancelled
                RegistrationStatusType.ADDED -> this
            }

        override fun getType(): RegistrationStatusType = RegistrationStatusType.REGISTERED
    }

    object Deregistered : RegistrationStatus() {
        override fun updateStatus(status: RegistrationStatusType): RegistrationStatus =
            when (status) {
                RegistrationStatusType.REGISTERED -> Registered
                RegistrationStatusType.DEREGISTERED -> this
                RegistrationStatusType.CANCELLED -> this
                RegistrationStatusType.ADDED -> this
            }

        override fun getType(): RegistrationStatusType = RegistrationStatusType.DEREGISTERED
    }

    object Cancelled : RegistrationStatus() {
        override fun updateStatus(status: RegistrationStatusType): RegistrationStatus =
            when (status) {
                RegistrationStatusType.REGISTERED -> this
                RegistrationStatusType.DEREGISTERED -> this
                RegistrationStatusType.CANCELLED -> this
                RegistrationStatusType.ADDED -> Added
            }

        override fun getType(): RegistrationStatusType = RegistrationStatusType.CANCELLED
    }

    object Added : RegistrationStatus() {
        override fun updateStatus(status: RegistrationStatusType): RegistrationStatus =
            when (status) {
                RegistrationStatusType.REGISTERED -> this
                RegistrationStatusType.DEREGISTERED -> Deregistered
                RegistrationStatusType.CANCELLED -> Cancelled
                RegistrationStatusType.ADDED -> this
            }

        override fun getType(): RegistrationStatusType = RegistrationStatusType.ADDED
    }
}
