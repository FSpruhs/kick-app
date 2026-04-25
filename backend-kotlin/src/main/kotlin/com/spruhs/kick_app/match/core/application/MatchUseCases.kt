package com.spruhs.kick_app.match.core.application

import com.spruhs.kick_app.common.es.AggregateStore
import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.es.UnknownEventTypeException
import com.spruhs.kick_app.common.exceptions.UserNotAuthorizedException
import com.spruhs.kick_app.common.helper.KeyedMutex
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.generateId
import com.spruhs.kick_app.group.api.GroupApi
import com.spruhs.kick_app.match.api.MatchApi
import com.spruhs.kick_app.match.api.MatchNumber
import com.spruhs.kick_app.match.api.MatchNumberChangedEvent
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.core.domain.AttendanceBased
import com.spruhs.kick_app.match.core.domain.EnterResultResponse
import com.spruhs.kick_app.match.core.domain.MatchAggregate
import com.spruhs.kick_app.match.core.domain.PlayerCount
import com.spruhs.kick_app.match.core.domain.PlayerOverview
import com.spruhs.kick_app.match.core.domain.Playground
import com.spruhs.kick_app.match.core.domain.RegistrationStatusType
import com.spruhs.kick_app.match.core.domain.RoundRobin
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MatchCommandPort(
    private val aggregateStore: AggregateStore,
    private val groupApi: GroupApi,
    private val mutex: KeyedMutex<MatchId> = KeyedMutex(),
    private val matchOverviewService: MatchOverviewService,
    private val playerOverviewService: PlayerOverviewService,
    private val matchApi: MatchApi,
) {
    suspend fun plan(command: PlanMatchCommand): MatchAggregate {
        require(groupApi.isActiveMember(command.groupId, command.requesterId)) {
            throw UserNotAuthorizedException(command.requesterId)
        }
        val matchId = generateId()
        val matchOverview = matchOverviewService.getMatchHistory(command.groupId)
        val lastMatchNumber = matchOverview.add(MatchId(matchId), command.start)

        return MatchAggregate(matchId).also {
            it.planMatch(
                groupId = command.groupId,
                start = command.start,
                playground = command.playground,
                playerCount = command.playerCount,
                lastMatchNumber = MatchNumber(lastMatchNumber),
            )
            aggregateStore.save(it)
            matchOverviewService.save(matchOverview)
        }
    }

    suspend fun cancelMatch(command: CancelMatchCommand) =
        handle(command.matchId) { match ->
            require(groupApi.isActiveCoach(match.groupId, command.userId)) {
                throw UserNotAuthorizedException(command.userId)
            }
            match.cancelMatch()
        }

    suspend fun changePlayground(command: ChangePlaygroundCommand) =
        handle(command.matchId) { match ->
            require(groupApi.isActiveCoach(match.groupId, command.userId)) {
                throw UserNotAuthorizedException(command.userId)
            }
            match.changePlayground(command.playground)
        }

    suspend fun addRegistration(command: AddRegistrationCommand) =
        handle(command.matchId) { match ->
            validateRegistrationRequest(command, match)
            val playerOverview = if (match.playerPriorityStrategy is RoundRobin || match.playerPriorityStrategy is AttendanceBased) {
                playerOverviewService.getOverviewEntry(match.groupId, command.updatedUser)
            } else null
            match.addRegistration(
                userId = command.updatedUser,
                registrationStatusType = command.status,
                guests = command.guests,
                playerOverview = playerOverview
            )
        }

    suspend fun enterResult(command: EnterResultCommand) {
        var overview: PlayerOverview? = null
        var groupId: GroupId? = null

        handle(command.matchId) { match ->
            require(groupApi.isActiveCoach(match.groupId, command.userId)) {
                throw UserNotAuthorizedException(command.userId)
            }
            groupId = match.groupId
            val result = match.enterResult(command.players)
            overview = playerOverviewService.getOverview(match.groupId).also { ov ->
                if (result is EnterResultResponse.FirstEntry) {
                    ov.enterResult(match)
                } else {
                    ov.updateResult(match)
                }
                playerOverviewService.save(ov)
            }
        }

        if (overview == null || groupId == null) {
            return
        }

        matchApi.findPlanningMatchIds(groupId).forEach { matchId ->
            handle(matchId) { match ->
                match.updatePlayerOverview(overview)
            }
        }
    }

    private suspend fun validateRegistrationRequest(
        command: AddRegistrationCommand,
        match: MatchAggregate,
    ) {
        require(groupApi.isActiveMember(match.groupId, command.updatedUser)) {
            throw UserNotAuthorizedException(command.updatingUser)
        }
        require(command.guests >= 0) {
            throw IllegalArgumentException("Guests cannot be negative")
        }
        when (command.status) {
            RegistrationStatusType.REGISTERED, RegistrationStatusType.DEREGISTERED -> {
                require(command.updatedUser == command.updatingUser) {
                    throw UserNotAuthorizedException(command.updatingUser)
                }
            }
            RegistrationStatusType.CANCELLED, RegistrationStatusType.ADDED -> {
                require(groupApi.isActiveCoach(match.groupId, command.updatingUser)) {
                    throw UserNotAuthorizedException(command.updatingUser)
                }
            }
        }
    }

    private suspend fun handleChangeMatchNumber(event: MatchNumberChangedEvent) =
        handle(MatchId(event.aggregateId)) { match ->
            match.apply(MatchNumberChangedEvent(event.aggregateId, event.newMatchNumber))
        }

    suspend fun onEvent(event: BaseEvent) {
        when (event) {
            is MatchNumberChangedEvent -> handleChangeMatchNumber(event)
            else -> throw UnknownEventTypeException(event)
        }
    }

    private suspend fun loadMatch(matchId: MatchId): MatchAggregate = aggregateStore.load(matchId.value, MatchAggregate::class.java)

    private suspend fun handle(
        id: MatchId,
        block: suspend (MatchAggregate) -> Unit,
    ) {
        mutex.withKeyLock(id) {
            loadMatch(id).also {
                block(it)
                aggregateStore.save(it)
            }
        }
    }
}

data class EnterResultCommand(
    val userId: UserId,
    val matchId: MatchId,
    val players: List<ParticipatingPlayer>,
)

data class CancelMatchCommand(
    val userId: UserId,
    val matchId: MatchId,
)

data class PlanMatchCommand(
    val requesterId: UserId,
    val groupId: GroupId,
    val start: LocalDateTime,
    val playground: Playground? = null,
    val playerCount: PlayerCount,
)

data class AddRegistrationCommand(
    val updatingUser: UserId,
    val updatedUser: UserId,
    val matchId: MatchId,
    val status: RegistrationStatusType,
    val guests: Int,
)

data class ChangePlaygroundCommand(
    val userId: UserId,
    val matchId: MatchId,
    val playground: Playground,
)
