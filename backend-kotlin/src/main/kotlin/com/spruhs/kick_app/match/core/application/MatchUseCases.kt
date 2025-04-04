package com.spruhs.kick_app.match.core.application

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.group.api.GroupApi
import com.spruhs.kick_app.match.core.adapter.secondary.MatchPersistenceAdapter
import com.spruhs.kick_app.match.core.domain.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MatchUseCases(
    private val matchPersistenceAdapter: MatchPersistenceAdapter,
    private val groupApi: GroupApi,
    private val eventPublisher: EventPublisher
) {
    suspend fun plan(command: PlanMatchCommand) {
        planMatch(
            groupId = command.groupId,
            start = command.start,
            playground = command.playground,
            playerCount = command.playerCount
        ).apply {
            matchPersistenceAdapter.save(this)
            eventPublisher.publishAll(this.domainEvents)
        }
    }

    suspend fun getMatchesByGroupId(groupId: GroupId, requestingUserId: UserId): List<Match> {
        require(groupApi.isActiveMember(groupId, requestingUserId)) { throw UserNotAuthorizedException(requestingUserId) }
        return matchPersistenceAdapter.findAllByGroupId(groupId).sortedByDescending { it.start }
    }

    suspend fun cancel(command: CancelMatchCommand) {
        val match = fetchMatch(command.matchId)
        require(groupApi.isActiveAdmin(match.groupId, command.userId)) {
            throw UserNotAuthorizedException(command.userId)
        }

        match.cancel().apply {
            matchPersistenceAdapter.save(this)
        }
    }

    suspend fun updatePlayerRegistration(command: UpdatePlayerRegistrationCommand) {
        val match = fetchMatch(command.matchId).apply {
            handleUpdatePlayerRegistration(this, command)
        }.apply {
            matchPersistenceAdapter.save(this)
        }
        matchPersistenceAdapter.save(match)
    }

    private suspend fun handleUpdatePlayerRegistration(match: Match, command: UpdatePlayerRegistrationCommand): Match {
        return if (command.updatingUser == command.updatedUser) {
            addPlayerRegistration(match, command)
        } else {
            updatePlayerRegistration(match, command)
        }
    }

    private suspend fun addPlayerRegistration(match: Match, command: UpdatePlayerRegistrationCommand): Match {
        require(groupApi.isActiveMember(match.groupId, command.updatingUser))
        return match.addRegistration(command.updatedUser, command.status)
    }

    private suspend fun updatePlayerRegistration(match: Match, command: UpdatePlayerRegistrationCommand): Match {
        require(groupApi.isActiveAdmin(match.groupId, command.updatingUser))
        return match.updateRegistration(command.updatedUser, command.status)
    }

    suspend fun addResult(command: AddResultCommand) {
        val match = fetchMatch(command.matchId)
        require(groupApi.isActiveAdmin(match.groupId, command.userId)) {
            throw UserNotAuthorizedException(command.userId)
        }

        require(groupApi.areActiveMembers(match.groupId, command.teamA + command.teamB)) {
            "Not all players are active members of the group"
        }

        match.addResult(command.result, command.teamA, command.teamB).apply {
            matchPersistenceAdapter.save(this)
            eventPublisher.publishAll(this.domainEvents)
        }
    }

    suspend fun getMatch(matchId: MatchId, requestingUserId: UserId): Match {
        val match = fetchMatch(matchId)
        require(groupApi.isActiveMember(match.groupId, requestingUserId)) {
            throw UserNotAuthorizedException(
                requestingUserId
            )
        }
        return match
    }

    private suspend fun fetchMatch(matchId: MatchId): Match =
        matchPersistenceAdapter.findById(matchId) ?: throw MatchNotFoundException(matchId)
}

data class AddResultCommand(
    val userId: UserId,
    val matchId: MatchId,
    val result: Result,
    val teamA: Set<UserId>,
    val teamB: Set<UserId>
)

data class CancelMatchCommand(
    val userId: UserId,
    val matchId: MatchId
)

data class PlanMatchCommand(
    val groupId: GroupId,
    val start: LocalDateTime,
    val playground: Playground,
    val playerCount: PlayerCount,
)

data class UpdatePlayerRegistrationCommand(
    val updatingUser: UserId,
    val updatedUser: UserId,
    val matchId: MatchId,
    val status: RegistrationStatus
)