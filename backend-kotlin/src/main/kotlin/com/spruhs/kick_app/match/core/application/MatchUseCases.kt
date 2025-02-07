package com.spruhs.kick_app.match.core.application

import com.spruhs.kick_app.common.EventPublisher
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.UserId
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

    fun addRegistration(command: AddRegistrationCommand) {
        val match = matchPersistenceAdapter.findById(command.matchId) ?: throw MatchNotFoundException(command.matchId)
        require(groupApi.isActiveMember(match.groupId, command.userId)) {
            "User is not an active member of the group"
        }

        match.addRegistration(command.userId, command.registrationStatus)
        matchPersistenceAdapter.save(match)
    }
}

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