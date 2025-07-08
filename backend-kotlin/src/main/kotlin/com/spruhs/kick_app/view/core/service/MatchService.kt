package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.exceptions.MatchNotFoundException
import com.spruhs.kick_app.common.es.UnknownEventTypeException
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.exceptions.UserNotAuthorizedException
import com.spruhs.kick_app.view.api.GroupApi
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.view.api.UserApi
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MatchService(
    private val repository: MatchProjectionRepository,
    private val groupApi: GroupApi,
    private val userApi: UserApi,
) {
    suspend fun whenEvent(event: BaseEvent) {
        when (event) {
            is MatchPlannedEvent -> handleMatchPlannedEvent(event)
            is PlayerAddedToCadreEvent -> handlePlayerAddedToCadreEvent(event)
            is PlayerDeregisteredEvent -> handlePlayerDeregisteredEvent(event)
            is PlayerPlacedOnWaitingBenchEvent -> handlePlayerPlacedOnWaitingBenchEvent(event)
            is MatchCanceledEvent -> handleMatchCanceledEvent(event)
            is PlaygroundChangedEvent -> handlePlaygroundChangedEvent(event)
            is MatchResultEnteredEvent -> handleMatchResultEnteredEvent(event)
            else -> throw UnknownEventTypeException(event)
        }
    }

    private suspend fun handleMatchPlannedEvent(event: MatchPlannedEvent) {
        repository.save(event.toProjection())
    }

    private suspend fun findMatch(matchId: MatchId): MatchProjection =
        repository.findById(matchId)
            ?: throw MatchNotFoundException(matchId)

    private suspend fun handlePlayerAddedToCadreEvent(event: PlayerAddedToCadreEvent) {
        findMatch(MatchId(event.aggregateId)).also {
            it.cadrePlayers += event.userId
            it.deregisteredPlayers -= event.userId
            it.waitingBenchPlayers -= event.userId
            repository.save(it)
        }
    }

    private suspend fun handlePlayerDeregisteredEvent(event: PlayerDeregisteredEvent) {
        findMatch(MatchId(event.aggregateId)).also {
            it.cadrePlayers -= event.userId
            it.deregisteredPlayers += event.userId
            it.waitingBenchPlayers -= event.userId
            repository.save(it)
        }
    }

    private suspend fun handlePlayerPlacedOnWaitingBenchEvent(event: PlayerPlacedOnWaitingBenchEvent) {
        findMatch(MatchId(event.aggregateId)).also {
            it.cadrePlayers -= event.userId
            it.deregisteredPlayers -= event.userId
            it.waitingBenchPlayers += event.userId
            repository.save(it)
        }
    }

    private suspend fun handleMatchCanceledEvent(event: MatchCanceledEvent) {
        findMatch(MatchId(event.aggregateId)).also {
            it.isCanceled = true
            repository.save(it)
        }
    }

    private suspend fun handlePlaygroundChangedEvent(event: PlaygroundChangedEvent) {
        findMatch(MatchId(event.aggregateId)).also {
            it.playground = event.newPlayground
            repository.save(it)
        }
    }

    private suspend fun handleMatchResultEnteredEvent(event: MatchResultEnteredEvent) {
        findMatch(MatchId(event.aggregateId)).also {
            it.result = event.players
            repository.save(it)
        }
    }

    suspend fun getMatch(matchId: MatchId, userId: UserId): MatchProjection {
        val match = findMatch(matchId)
        require(groupApi.isActiveMember(match.groupId, userId)) { throw UserNotAuthorizedException(userId) }
        return match
    }

    suspend fun getMatchesByGroup(
        groupId: GroupId,
        userId: UserId,
        matchFilter: MatchFilter,
    ): List<MatchProjection> {
        require(groupApi.isActiveMember(groupId, userId)) { throw UserNotAuthorizedException(userId) }
        return repository.findAllByGroupId(groupId, matchFilter)
    }

    suspend fun getPlayerMatches(
        playerId: UserId,
        after: LocalDateTime? = null,
    ): List<MatchProjection> =
        userApi.getGroups(playerId).flatMap { group ->
            repository.findAllByGroupId(group, MatchFilter(after = after))
        }
}

interface MatchProjectionRepository {
    suspend fun save(matchProjection: MatchProjection)
    suspend fun findById(matchId: MatchId): MatchProjection?
    suspend fun findAllByGroupId(groupId: GroupId, filter: MatchFilter): List<MatchProjection>
}

data class MatchProjection(
    val id: MatchId,
    val groupId: GroupId,
    val start: LocalDateTime,
    var playground: String? = null,
    var isCanceled: Boolean,
    val maxPlayer: Int,
    val minPlayer: Int,
    var cadrePlayers: Set<UserId>,
    var waitingBenchPlayers: Set<UserId>,
    var deregisteredPlayers: Set<UserId>,
    var result: List<ParticipatingPlayer>,
)

data class MatchFilter(
    val after: LocalDateTime? = null,
    val before: LocalDateTime? = null,
    val limit: Int? = null
) {
    init {
        require(after == null || before == null || after.isBefore(before)) {
            throw IllegalArgumentException("After date must be before before date")
        }
    }
}

private fun MatchPlannedEvent.toProjection(): MatchProjection =
    MatchProjection(
        id = MatchId(this.aggregateId),
        groupId = this.groupId,
        start = this.start,
        playground = this.playground,
        maxPlayer = this.maxPlayer,
        minPlayer = this.minPlayer,
        isCanceled = false,
        cadrePlayers = emptySet(),
        deregisteredPlayers = emptySet(),
        waitingBenchPlayers = emptySet(),
        result = emptyList()
    )