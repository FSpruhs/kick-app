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

    private suspend fun handleResultEntered(event: MatchResultEnteredEvent) {
        val oldResult = resultRepository.findByMatchId(MatchId(event.aggregateId))
        val newResult = event.players.associate { it.userId to PlayerResultProjection(it.playerResult, it.team) }
        if (oldResult == null) {

            event.players.forEach { player ->
                val playerStatistic = statisticRepository.findByPlayer(event.groupId, player.userId)
                    ?: PlayerStatisticProjection(
                        id = generateId(),
                        groupId = event.groupId,
                        userId = player.userId
                    )
                playerStatistic.totalMatches += 1
                when (player.playerResult) {
                    PlayerResult.WIN -> playerStatistic.wins += 1
                    PlayerResult.LOSS -> playerStatistic.losses += 1
                    PlayerResult.DRAW -> playerStatistic.draws += 1
                }
                statisticRepository.save(playerStatistic)
            }

            resultRepository.save(
                ResultProjection(
                    id = generateId(),
                    matchId = MatchId(event.aggregateId),
                    players = event.players.associate { it.userId to PlayerResultProjection(it.playerResult, it.team) },
                )
            )

        } else {

            event.players.forEach { player ->
                val oldPlayer = oldResult.players[player.userId]
                if (oldPlayer == null) {
                    val playerStatistic = statisticRepository.findByPlayer(event.groupId, player.userId)
                        ?: PlayerStatisticProjection(
                            id = generateId(),
                            groupId = event.groupId,
                            userId = player.userId
                        )
                    playerStatistic.totalMatches += 1
                    when (player.playerResult) {
                        PlayerResult.WIN -> playerStatistic.wins += 1
                        PlayerResult.LOSS -> playerStatistic.losses += 1
                        PlayerResult.DRAW -> playerStatistic.draws += 1
                    }
                    statisticRepository.save(playerStatistic)
                } else {
                    if (player.playerResult != oldPlayer.matchResult) {
                        val playerStatistic = statisticRepository.findByPlayer(event.groupId, player.userId)
                            ?: throw PlayerNotFoundException(player.userId)
                        when (player.playerResult) {
                            PlayerResult.WIN -> playerStatistic.wins += 1
                            PlayerResult.LOSS -> playerStatistic.losses += 1
                            PlayerResult.DRAW -> playerStatistic.draws += 1
                        }
                        when (oldPlayer.matchResult) {
                            PlayerResult.WIN -> playerStatistic.wins -= 1
                            PlayerResult.LOSS -> playerStatistic.losses -= 1
                            PlayerResult.DRAW -> playerStatistic.draws -= 1
                        }
                        statisticRepository.save(playerStatistic)
                    }
                }
            }

            oldResult.players.keys.forEach { oldPlayer ->
                val player = newResult[oldPlayer]
                if (player == null) {
                    val playerStatistic = statisticRepository.findByPlayer(event.groupId, oldPlayer)
                        ?: throw PlayerNotFoundException(oldPlayer)
                    playerStatistic.totalMatches -= 1
                    when (oldResult.players[oldPlayer]!!.matchResult) {
                        PlayerResult.WIN -> playerStatistic.wins -= 1
                        PlayerResult.LOSS -> playerStatistic.losses -= 1
                        PlayerResult.DRAW -> playerStatistic.draws -= 1
                    }
                    statisticRepository.save(playerStatistic)
                }
            }

            resultRepository.save(
                ResultProjection(
                    id = oldResult.id,
                    matchId = MatchId(event.aggregateId),
                    players = event.players.associate { it.userId to PlayerResultProjection(it.playerResult, it.team) },
                )
            )
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