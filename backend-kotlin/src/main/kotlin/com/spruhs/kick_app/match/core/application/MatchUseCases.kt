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
    fun plan(command: PlanMatchCommand) {
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

    fun cancel(command: CancelMatchCommand) {
        val match = fetchMatch(command.matchId)
        require(groupApi.isActiveAdmin(match.groupId, command.userId)) {
            throw UserNotAuthorizedException(command.userId)
        }

        match.cancel().apply {
            matchPersistenceAdapter.save(this)
        }
    }

    fun cancelPlayer(command: CancelPlayerCommand) {
        val match = fetchMatch(command.matchId)
        require(groupApi.isActiveAdmin(match.groupId, command.cancelingUserId)) {
            throw UserNotAuthorizedException(command.userId)
        }

        match.cancelPlayer(command.userId).apply {
            matchPersistenceAdapter.save(this)
        }
    }

    fun addRegistration(command: AddRegistrationCommand) {
        val match = fetchMatch(command.matchId)
        require(groupApi.isActiveMember(match.groupId, command.userId)) {
            throw UserNotAuthorizedException(command.userId)
        }

        match.addRegistration(command.userId, command.registrationStatus)
        matchPersistenceAdapter.save(match)
    }

    fun addResult(command: AddResultCommand) {
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

    private fun fetchMatch(matchId: MatchId): Match =
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

data class AddRegistrationCommand(
    val userId: UserId,
    val matchId: MatchId,
    val registrationStatus: RegistrationStatus
)

data class PlanMatchCommand(
    val groupId: GroupId,
    val start: LocalDateTime,
    val playground: Playground,
    val playerCount: PlayerCount,
)

data class CancelPlayerCommand(
    val userId: UserId,
    val cancelingUserId: UserId,
    val matchId: MatchId
)