package com.spruhs.kick_app.viewservice.core.service

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.Result
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.UnknownEventTypeException
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.group.api.GroupApi
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.match.core.domain.MatchNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MatchService(
    private val repository: MatchProjectionRepository,
    private val groupApi: GroupApi
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
        val matchProjection = MatchProjection(
            id = MatchId(event.aggregateId),
            groupId = event.groupId,
            start = event.start,
            playground = event.playground,
            maxPlayer = event.maxPlayer,
            minPlayer = event.minPlayer,
            isCanceled = false,
            cadrePlayers = emptySet(),
            deregisteredPlayers = emptySet(),
            waitingBenchPlayers = emptySet(),
            teamA = emptySet(),
            teamB = emptySet(),
            result = null
        )
        repository.save(matchProjection)
    }

    private suspend fun findMatch(matchId: MatchId): MatchProjection =
        repository.findById(matchId)
            ?: throw MatchNotFoundException(matchId)

    private suspend fun handlePlayerAddedToCadreEvent(event: PlayerAddedToCadreEvent) {
        val match = findMatch(MatchId(event.aggregateId))
        match.cadrePlayers += event.userId
        match.deregisteredPlayers -= event.userId
        match.waitingBenchPlayers -= event.userId
        repository.save(match)
    }

    private suspend fun handlePlayerDeregisteredEvent(event: PlayerDeregisteredEvent) {
        val match = findMatch(MatchId(event.aggregateId))
        match.cadrePlayers -= event.userId
        match.deregisteredPlayers += event.userId
        match.waitingBenchPlayers -= event.userId
        repository.save(match)
    }

    private suspend fun handlePlayerPlacedOnWaitingBenchEvent(event: PlayerPlacedOnWaitingBenchEvent) {
        val match = findMatch(MatchId(event.aggregateId))
        match.cadrePlayers -= event.userId
        match.deregisteredPlayers -= event.userId
        match.waitingBenchPlayers += event.userId
        repository.save(match)
    }

    private suspend fun handleMatchCanceledEvent(event: MatchCanceledEvent) {
        val match = findMatch(MatchId(event.aggregateId))
        match.isCanceled = true
        repository.save(match)
    }

    private suspend fun handlePlaygroundChangedEvent(event: PlaygroundChangedEvent) {
        val match = findMatch(MatchId(event.aggregateId))
        match.playground = event.newPlayground
        repository.save(match)
    }

    private suspend fun handleMatchResultEnteredEvent(event: MatchResultEnteredEvent) {
        val match = findMatch(MatchId(event.aggregateId))
        match.result = event.result
        match.teamA = event.teamA.toSet()
        match.teamB = event.teamB.toSet()
        repository.save(match)
    }

    suspend fun getMatch(matchId: MatchId, userId: UserId): Pair<MatchProjection, Map<UserId, String>> {
        val match = repository.findById(matchId) ?: throw MatchNotFoundException(matchId)
        require(groupApi.isActiveMember(match.groupId, userId)) { throw UserNotAuthorizedException(userId) }
        val groupNameList = groupApi.getGroupNameList(match.groupId)
        return Pair(match, groupNameList)
    }

    suspend fun getMatchesByGroup(groupId: GroupId, userId: UserId): List<MatchProjection> {
        require(groupApi.isActiveMember(groupId, userId)) { throw UserNotAuthorizedException(userId) }
        return repository.findAllByGroupId(groupId)
    }

    suspend fun getPlayerMatches(playerId: UserId, after: LocalDateTime? = null): Pair<List<MatchProjection>, Map<UserId, String>> {
        return Pair(emptyList(), emptyMap())
    }
}

interface MatchProjectionRepository {
    suspend fun save(matchProjection: MatchProjection)
    suspend fun findById(matchId: MatchId): MatchProjection?
    suspend fun findAllByGroupId(groupId: GroupId): List<MatchProjection>
}

data class MatchProjection(
    val id: MatchId,
    val groupId: GroupId,
    val start: LocalDateTime,
    var playground: String?,
    var isCanceled: Boolean,
    val maxPlayer: Int,
    val minPlayer: Int,
    var cadrePlayers: Set<UserId>,
    var waitingBenchPlayers: Set<UserId>,
    var deregisteredPlayers: Set<UserId>,
    var teamA: Set<UserId>,
    var teamB: Set<UserId>,
    var result: Result? = null,
)