package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.UnknownEventTypeException
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.common.generateId
import com.spruhs.kick_app.group.api.GroupCreatedEvent
import com.spruhs.kick_app.group.api.PlayerEnteredGroupEvent
import com.spruhs.kick_app.group.core.domain.PlayerNotFoundException
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.MatchTeam
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.api.PlayerResult
import com.spruhs.kick_app.view.api.GroupApi
import org.springframework.stereotype.Service

@Service
class StatisticService(
    private val statisticRepository: StatisticProjectionRepository,
    private val resultRepository: ResultProjectionRepository,
    private val groupApi: GroupApi
) {

    suspend fun whenEvent(event: BaseEvent) {
        when (event) {
            is GroupCreatedEvent -> handleNewPlayer(GroupId(event.aggregateId), event.userId)
            is PlayerEnteredGroupEvent -> handleNewPlayer(GroupId(event.aggregateId), event.userId)
            is MatchResultEnteredEvent -> handleResultEntered(event)
            else -> UnknownEventTypeException(event)
        }
    }

    private suspend fun handleNewPlayer(groupId: GroupId, userId: UserId) {
        val player = statisticRepository.findByPlayer(groupId, userId)
        if (player == null) {
            PlayerStatisticProjection(
                id = generateId(),
                groupId = groupId,
                userId = userId
            ).also {
                statisticRepository.save(it)
            }
        }
    }

    private suspend fun findPlayerStatisticOrCreateNew(
        groupId: GroupId,
        userId: UserId
    ): PlayerStatisticProjection = statisticRepository.findByPlayer(groupId, userId)
        ?: PlayerStatisticProjection(
            id = generateId(),
            groupId = groupId,
            userId = userId
        )


    private suspend fun handleFirstResultEntered(event: MatchResultEnteredEvent) {
        event.players.forEach { player ->
            handelNewPlayerEnteredOldMatch(event.groupId, player)
        }
    }

    private suspend fun handelNewPlayerEnteredOldMatch(groupId: GroupId, player: ParticipatingPlayer) {
        findPlayerStatisticOrCreateNew(groupId, player.userId).also {
            it.totalMatches += 1
            when (player.playerResult) {
                PlayerResult.WIN -> it.wins += 1
                PlayerResult.LOSS -> it.losses += 1
                PlayerResult.DRAW -> it.draws += 1
            }
            statisticRepository.save(it)
        }
    }

    private suspend fun findPlayerStatisticOrThrow(
        groupId: GroupId,
        userId: UserId
    ): PlayerStatisticProjection = statisticRepository.findByPlayer(groupId, userId)
        ?: throw PlayerNotFoundException(userId)

    private suspend fun handelOldPlayerResultInOldMatch(
        player: ParticipatingPlayer,
        oldPlayer: PlayerResultProjection,
        groupId: GroupId
    ) {
        if (player.playerResult != oldPlayer.matchResult) {
            findPlayerStatisticOrThrow(groupId, player.userId).apply {
                when (player.playerResult) {
                    PlayerResult.WIN -> this.wins += 1
                    PlayerResult.LOSS -> this.losses += 1
                    PlayerResult.DRAW -> this.draws += 1
                }
                when (oldPlayer.matchResult) {
                    PlayerResult.WIN -> this.wins -= 1
                    PlayerResult.LOSS -> this.losses -= 1
                    PlayerResult.DRAW -> this.draws -= 1
                }
                statisticRepository.save(this)
            }
        }
    }

    private suspend fun handelPlayerInEnteredResult(event: MatchResultEnteredEvent, oldResult: ResultProjection) {
        event.players.forEach { player ->
            val oldPlayer = oldResult.players[player.userId]
            if (oldPlayer == null) {
                handelNewPlayerEnteredOldMatch(event.groupId, player)
            } else {
                handelOldPlayerResultInOldMatch(player, oldPlayer, event.groupId)
            }
        }
    }

    private suspend fun handelPlayerNoLongerInResult(event: MatchResultEnteredEvent, oldResult: ResultProjection) {
        val newResult = event.toPlayerMap()
        oldResult.players.keys.forEach { oldPlayer ->
            val player = newResult[oldPlayer]
            if (player == null) {
                findPlayerStatisticOrThrow(event.groupId, oldPlayer).apply {
                    this.totalMatches -= 1
                    when (oldResult.players[oldPlayer]!!.matchResult) {
                        PlayerResult.WIN -> this.wins -= 1
                        PlayerResult.LOSS -> this.losses -= 1
                        PlayerResult.DRAW -> this.draws -= 1
                    }
                    statisticRepository.save(this)
                }
            }
        }
    }

    private suspend fun handleAnotherResultEntered(event: MatchResultEnteredEvent, oldResult: ResultProjection) {
        handelPlayerInEnteredResult(event, oldResult)
        handelPlayerNoLongerInResult(event, oldResult)
        saveResult(event, oldResult.id)
    }

    private suspend fun saveResult(event: MatchResultEnteredEvent, id: String = generateId()) {
        resultRepository.save(
            ResultProjection(
                id = id,
                matchId = MatchId(event.aggregateId),
                players = event.toPlayerMap()
            )
        )
    }

    private suspend fun handleResultEntered(event: MatchResultEnteredEvent) {
        val oldResult = resultRepository.findByMatchId(MatchId(event.aggregateId))
        if (oldResult == null) {
            handleFirstResultEntered(event)
            saveResult(event)
        } else {
            handleAnotherResultEntered(event, oldResult)
        }
    }

    suspend fun getPlayerStatistics(
        groupId: GroupId,
        userId: UserId,
        requestingUserId: UserId
    ): PlayerStatisticProjection {
        require(groupApi.isActiveMember(groupId, requestingUserId)) {
            throw UserNotAuthorizedException(requestingUserId)
        }

        return statisticRepository.findByPlayer(groupId, userId) ?: throw PlayerNotFoundException(userId)
    }
}

interface StatisticProjectionRepository {
    suspend fun findByPlayer(groupId: GroupId, userId: UserId): PlayerStatisticProjection?
    suspend fun save(statistic: PlayerStatisticProjection)
}

interface ResultProjectionRepository {
    suspend fun findByMatchId(matchId: MatchId): ResultProjection?
    suspend fun save(result: ResultProjection)
}

data class PlayerStatisticProjection(
    val id: String,
    val groupId: GroupId,
    val userId: UserId,
    var totalMatches: Int = 0,
    var wins: Int = 0,
    var losses: Int = 0,
    var draws: Int = 0,
)

data class ResultProjection(
    val id: String,
    val matchId: MatchId,
    val players: Map<UserId, PlayerResultProjection>
)

data class PlayerResultProjection(
    val matchResult: PlayerResult,
    val team: MatchTeam
)

private fun MatchResultEnteredEvent.toPlayerMap(): Map<UserId, PlayerResultProjection> =
    this.players.associate { it.userId to PlayerResultProjection(it.playerResult, it.team) }