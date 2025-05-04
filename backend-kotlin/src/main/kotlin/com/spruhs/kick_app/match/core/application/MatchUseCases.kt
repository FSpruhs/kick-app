package com.spruhs.kick_app.match.core.application

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.group.api.GroupApi
import com.spruhs.kick_app.match.core.domain.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MatchCommandPort(
    private val aggregateStore: AggregateStore,
    private val groupApi: GroupApi,
) {
    suspend fun plan(command: PlanMatchCommand): MatchAggregate {
        require(groupApi.isActiveMember(command.groupId, command.requesterId)) {
            throw UserNotAuthorizedException(command.requesterId)
        }

        return MatchAggregate(generateId()).also {
            it.planMatch(command)
            aggregateStore.save(it)
        }
    }

    suspend fun cancelMatch(command: CancelMatchCommand) {
        val match = aggregateStore.load(command.matchId.value, MatchAggregate::class.java)
        require(groupApi.isActiveAdmin(match.groupId, command.userId)) {
            throw UserNotAuthorizedException(command.userId)
        }
        match.cancelMatch()
        aggregateStore.save(match)
    }

    suspend fun changePlayground(command: ChangePlaygroundCommand) {
        val match = aggregateStore.load(command.matchId.value, MatchAggregate::class.java)
        require(groupApi.isActiveAdmin(match.groupId, command.userId)) {
            throw UserNotAuthorizedException(command.userId)
        }
        match.changePlayground(command.playground)
        aggregateStore.save(match)
    }

    suspend fun addRegistration(command: AddRegistrationCommand) {
        val match = aggregateStore.load(command.matchId.value, MatchAggregate::class.java)
        require(groupApi.isActiveMember(match.groupId, command.updatedUser)) {
            throw UserNotAuthorizedException(command.updatingUser)
        }
        when (command.status) {
            RegistrationStatusType.REGISTERED -> {
                require(command.updatedUser == command.updatingUser) {}
            }

            RegistrationStatusType.DEREGISTERED -> {
                require(command.updatedUser == command.updatingUser) {}
            }

            RegistrationStatusType.CANCELLED -> {
                require(groupApi.isActiveAdmin(match.groupId, command.updatingUser)) {}
            }

            RegistrationStatusType.ADDED -> {
                require(groupApi.isActiveAdmin(match.groupId, command.updatingUser)) {}
            }
        }

        match.addRegistration(command.updatedUser, command.status)
        aggregateStore.save(match)
    }

    suspend fun enterResult(command: EnterResultCommand) {
        val match = aggregateStore.load(command.matchId.value, MatchAggregate::class.java)
        require(groupApi.isActiveAdmin(match.groupId, command.userId)) {
            throw UserNotAuthorizedException(command.userId)
        }

        match.enterResult(
            result = command.result,
            participatingPlayer = command.teamA.map { ParticipatingPlayer(it, Team.A) } + command.teamB.map { ParticipatingPlayer(it, Team.B) }
        )

        aggregateStore.save(match)
    }

    suspend fun startMatches(time: LocalDateTime) {

    }
}

@Service
class MatchQueryPort(
    private val projectionPort: MatchProjectionPort,
    private val groupApi: GroupApi,
) {
    suspend fun getMatch(matchId: MatchId, userId: UserId): MatchProjection {
        val match = projectionPort.findById(matchId) ?: throw MatchNotFoundException(matchId)
        require(groupApi.isActiveMember(match.groupId, userId)) { throw UserNotAuthorizedException(userId) }
        return match
    }

    suspend fun getMatchesByGroup(groupId: GroupId, userId: UserId): List<MatchProjection> {
        require(groupApi.isActiveMember(groupId, userId)) { throw UserNotAuthorizedException(userId) }
        return projectionPort.findAllByGroupId(groupId)
    }
}

data class EnterResultCommand(
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
    val requesterId: UserId,
    val groupId: GroupId,
    val start: LocalDateTime,
    val playground: Playground,
    val playerCount: PlayerCount,
)

data class AddRegistrationCommand(
    val updatingUser: UserId,
    val updatedUser: UserId,
    val matchId: MatchId,
    val status: RegistrationStatusType
)

data class ChangePlaygroundCommand(
    val userId: UserId,
    val matchId: MatchId,
    val playground: Playground
)