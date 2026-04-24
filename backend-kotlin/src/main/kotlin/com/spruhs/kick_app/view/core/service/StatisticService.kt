package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.es.UnknownEventTypeException
import com.spruhs.kick_app.common.exceptions.PlayerNotFoundException
import com.spruhs.kick_app.common.exceptions.UserNotAuthorizedException
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.generateId
import com.spruhs.kick_app.group.api.GroupApi
import com.spruhs.kick_app.group.api.GroupCreatedEvent
import com.spruhs.kick_app.group.api.PlayerEnteredGroupEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.MatchResultUpdatedEvent
import com.spruhs.kick_app.match.api.MatchTeam
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.api.PlayerResult
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class StatisticService(
    private val statisticRepository: StatisticProjectionRepository,
    private val groupApi: GroupApi,
) {
    suspend fun whenEvent(event: BaseEvent) {
        when (event) {
            is GroupCreatedEvent -> handleNewPlayer(GroupId(event.aggregateId), event.userId)
            is PlayerEnteredGroupEvent -> handleNewPlayer(GroupId(event.aggregateId), event.userId)
            is MatchResultEnteredEvent -> handleResultEntered(event)
            is MatchResultUpdatedEvent -> handleMatchResultUpdated(event)
            else -> throw UnknownEventTypeException(event)
        }
    }

    private suspend fun handleMatchResultUpdated(event: MatchResultUpdatedEvent) {
        val statistic = findPlayerStatisticOrCreateNew(event.groupId, event.user)

        when {
            event.oldResult == null && event.newResult != null -> {
                statistic.totalMatches += 1
                when (event.newResult) {
                    PlayerResult.WIN -> statistic.wins += 1
                    PlayerResult.LOSS -> statistic.losses += 1
                    PlayerResult.DRAW -> statistic.draws += 1
                }
            }

            event.oldResult != null && event.newResult == null -> {
                statistic.totalMatches -= 1
                when (event.oldResult) {
                    PlayerResult.WIN -> statistic.wins -= 1
                    PlayerResult.LOSS -> statistic.losses -= 1
                    PlayerResult.DRAW -> statistic.draws -= 1
                }
            }

            event.oldResult != null && event.newResult != null && event.oldResult != event.newResult -> {
                when (event.oldResult) {
                    PlayerResult.WIN -> statistic.wins -= 1
                    PlayerResult.LOSS -> statistic.losses -= 1
                    PlayerResult.DRAW -> statistic.draws -= 1
                }
                when (event.newResult) {
                    PlayerResult.WIN -> statistic.wins += 1
                    PlayerResult.LOSS -> statistic.losses += 1
                    PlayerResult.DRAW -> statistic.draws += 1
                }
            }

            else -> return
        }

        statisticRepository.save(statistic)
    }

    private suspend fun handleNewPlayer(
        groupId: GroupId,
        userId: UserId,
    ) {
        val player = statisticRepository.findByPlayer(groupId, userId)
        if (player == null) {
            PlayerStatisticProjection(
                id = generateId(),
                groupId = groupId,
                userId = userId,
            ).also {
                statisticRepository.save(it)
            }
        }
    }

    private suspend fun findPlayerStatisticOrCreateNew(
        groupId: GroupId,
        userId: UserId,
    ): PlayerStatisticProjection =
        statisticRepository.findByPlayer(groupId, userId)
            ?: PlayerStatisticProjection(
                id = generateId(),
                groupId = groupId,
                userId = userId,
            )

    private suspend fun handleFirstResultEntered(event: MatchResultEnteredEvent) =
        coroutineScope {
            event.players.forEach { player ->
                launch {
                    handelNewPlayerEnteredOldMatch(event.groupId, player)
                }
            }
        }

    private suspend fun handelNewPlayerEnteredOldMatch(
        groupId: GroupId,
        player: ParticipatingPlayer,
    ) {
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

    private suspend fun handleResultEntered(event: MatchResultEnteredEvent) {
        handleFirstResultEntered(event)
    }

    suspend fun getPlayerStatistics(
        groupId: GroupId,
        userId: UserId,
        requestingUserId: UserId,
    ): PlayerStatisticProjection {
        require(groupApi.isActiveMember(groupId, requestingUserId)) {
            throw UserNotAuthorizedException(requestingUserId)
        }

        return statisticRepository.findByPlayer(groupId, userId) ?: throw PlayerNotFoundException(userId)
    }
}

interface StatisticProjectionRepository {
    suspend fun findByPlayer(
        groupId: GroupId,
        userId: UserId,
    ): PlayerStatisticProjection?

    suspend fun save(statistic: PlayerStatisticProjection)
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
    val players: Map<UserId, PlayerResultProjection>,
)

data class PlayerResultProjection(
    val matchResult: PlayerResult,
    val team: MatchTeam,
)
