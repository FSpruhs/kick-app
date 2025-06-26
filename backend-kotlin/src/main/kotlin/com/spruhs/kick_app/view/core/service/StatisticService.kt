package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.Result
import com.spruhs.kick_app.common.UnknownEventTypeException
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.common.generateId
import com.spruhs.kick_app.group.api.GroupCreatedEvent
import com.spruhs.kick_app.group.api.PlayerEnteredGroupEvent
import com.spruhs.kick_app.group.core.domain.PlayerNotFoundException
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
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
        if (oldResult == null) {

            event.teamA.forEach { player ->
                val playerStatistic = statisticRepository.findByPlayer(event.groupId, player)
                    ?: throw PlayerNotFoundException(player)
                playerStatistic.totalMatches += 1
                when (event.result) {
                    Result.WINNER_TEAM_A -> playerStatistic.wins += 1
                    Result.WINNER_TEAM_B -> playerStatistic.losses += 1
                    Result.DRAW -> playerStatistic.draws += 1
                }
            }

            event.teamB.forEach { player ->
                val playerStatistic = statisticRepository.findByPlayer(event.groupId, player)
                    ?: throw PlayerNotFoundException(player)
                playerStatistic.totalMatches += 1
                when (event.result) {
                    Result.WINNER_TEAM_B -> playerStatistic.wins += 1
                    Result.WINNER_TEAM_A -> playerStatistic.losses += 1
                    Result.DRAW -> playerStatistic.draws += 1
                }
            }

            resultRepository.save(
                ResultProjection(
                    id = generateId(),
                    matchId = MatchId(event.aggregateId),
                    teamA = event.teamA,
                    teamB = event.teamB,
                    result = event.result
                )
            )
        } else {

            event.teamA.forEach { player ->
                val teamAPlayer = oldResult.teamA.find { oldPlayer -> oldPlayer == player }
                if (teamAPlayer != null) {
                    if (event.result != oldResult.result) {
                        val playerStatistic = statisticRepository.findByPlayer(event.groupId, player)
                            ?: throw PlayerNotFoundException(player)
                        when (event.result) {
                            Result.WINNER_TEAM_A -> playerStatistic.wins += 1
                            Result.WINNER_TEAM_B -> playerStatistic.losses += 1
                            Result.DRAW -> playerStatistic.draws += 1
                        }
                        when (oldResult.result) {
                            Result.WINNER_TEAM_A -> playerStatistic.wins -= 1
                            Result.WINNER_TEAM_B -> playerStatistic.losses -= 1
                            Result.DRAW -> playerStatistic.draws -= 1
                        }
                    }
                }
            }

            event.teamB.forEach { player ->
                val teamBPlayer = oldResult.teamB.find { oldPlayer -> oldPlayer == player }
                if (teamBPlayer != null) {
                    if (event.result != oldResult.result) {
                        val playerStatistic = statisticRepository.findByPlayer(event.groupId, player)
                            ?: throw PlayerNotFoundException(player)
                        when (event.result) {
                            Result.WINNER_TEAM_B -> playerStatistic.wins += 1
                            Result.WINNER_TEAM_A -> playerStatistic.losses += 1
                            Result.DRAW -> playerStatistic.draws += 1
                        }
                        when (oldResult.result) {
                            Result.WINNER_TEAM_B -> playerStatistic.wins -= 1
                            Result.WINNER_TEAM_A -> playerStatistic.losses -= 1
                            Result.DRAW -> playerStatistic.draws -= 1
                        }
                    }
                }
            }

            event.teamA.forEach { player ->
                if (!oldResult.teamA.contains(player) && !oldResult.teamB.contains(player)) {
                    val playerStatistic = statisticRepository.findByPlayer(event.groupId, player)
                        ?: throw PlayerNotFoundException(player)
                    playerStatistic.totalMatches += 1
                    when (event.result) {
                        Result.WINNER_TEAM_A -> playerStatistic.wins += 1
                        Result.WINNER_TEAM_B -> playerStatistic.losses += 1
                        Result.DRAW -> playerStatistic.draws += 1
                    }
                }
            }

            event.teamB.forEach { player ->
                if (!oldResult.teamA.contains(player) && !oldResult.teamB.contains(player)) {
                    val playerStatistic = statisticRepository.findByPlayer(event.groupId, player)
                        ?: throw PlayerNotFoundException(player)
                    playerStatistic.totalMatches += 1
                    when (event.result) {
                        Result.WINNER_TEAM_B -> playerStatistic.wins += 1
                        Result.WINNER_TEAM_A -> playerStatistic.losses += 1
                        Result.DRAW -> playerStatistic.draws += 1
                    }
                }
            }

            oldResult.teamA.forEach { player ->
                if (!event.teamA.contains(player) && !event.teamB.contains(player)) {
                    val playerStatistic = statisticRepository.findByPlayer(event.groupId, player)
                        ?: throw PlayerNotFoundException(player)
                    playerStatistic.totalMatches -= 1
                    when (oldResult.result) {
                        Result.WINNER_TEAM_A -> playerStatistic.wins -= 1
                        Result.WINNER_TEAM_B -> playerStatistic.losses -= 1
                        Result.DRAW -> playerStatistic.draws -= 1
                    }
                }
            }

            oldResult.teamB.forEach { player ->
                if (!event.teamA.contains(player) && !event.teamB.contains(player)) {
                    val playerStatistic = statisticRepository.findByPlayer(event.groupId, player)
                        ?: throw PlayerNotFoundException(player)
                    playerStatistic.totalMatches -= 1
                    when (oldResult.result) {
                        Result.WINNER_TEAM_B -> playerStatistic.wins -= 1
                        Result.WINNER_TEAM_A -> playerStatistic.losses -= 1
                        Result.DRAW -> playerStatistic.draws -= 1
                    }
                }
            }

            resultRepository.save(
                ResultProjection(
                    id = oldResult.id,
                    matchId = MatchId(event.aggregateId),
                    teamA = event.teamA,
                    teamB = event.teamB,
                    result = event.result
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
    val teamA: List<UserId>,
    val teamB: List<UserId>,
    val result: Result,
)