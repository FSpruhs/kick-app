package com.spruhs.kick_app.match.core.application

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.view.api.GroupApi
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
        require(groupApi.isActiveCoach(match.groupId, command.userId)) {
            throw UserNotAuthorizedException(command.userId)
        }
        match.cancelMatch()
        aggregateStore.save(match)
    }

    suspend fun changePlayground(command: ChangePlaygroundCommand) {
        val match = aggregateStore.load(command.matchId.value, MatchAggregate::class.java)
        require(groupApi.isActiveCoach(match.groupId, command.userId)) {
            throw UserNotAuthorizedException(command.userId)
        }
        match.changePlayground(command.playground)
        aggregateStore.save(match)
    }

    private suspend fun validateRegistrationRequest(command: AddRegistrationCommand, match: MatchAggregate) {
        require(groupApi.isActiveMember(match.groupId, command.updatedUser)) {
            throw UserNotAuthorizedException(command.updatingUser)
        }
        when (command.status) {
            RegistrationStatusType.REGISTERED -> {
                require(command.updatedUser == command.updatingUser) {
                    throw UserNotAuthorizedException(command.updatingUser)
                }
            }

            RegistrationStatusType.DEREGISTERED -> {
                require(command.updatedUser == command.updatingUser) {
                    throw UserNotAuthorizedException(command.updatingUser)
                }
            }

            RegistrationStatusType.CANCELLED -> {
                require(groupApi.isActiveCoach(match.groupId, command.updatingUser)) {
                    throw UserNotAuthorizedException(command.updatingUser)
                }
            }

            RegistrationStatusType.ADDED -> {
                require(groupApi.isActiveCoach(match.groupId, command.updatingUser)) {
                    throw UserNotAuthorizedException(command.updatingUser)
                }
            }
        }
    }

    suspend fun addRegistration(command: AddRegistrationCommand) {
        val match = aggregateStore.load(command.matchId.value, MatchAggregate::class.java)
        validateRegistrationRequest(command, match)
        match.addRegistration(command.updatedUser, command.status)
        aggregateStore.save(match)
    }

    suspend fun enterResult(command: EnterResultCommand) {
        val match = aggregateStore.load(command.matchId.value, MatchAggregate::class.java)
        require(groupApi.isActiveCoach(match.groupId, command.userId)) {
            throw UserNotAuthorizedException(command.userId)
        }

        match.enterResult(command.players)

        aggregateStore.save(match)
    }
}

data class EnterResultCommand(
    val userId: UserId,
    val matchId: MatchId,
    val players: List<ParticipatingPlayer>
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