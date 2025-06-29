package com.spruhs.kick_app.match

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.match.api.MatchTeam
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.api.PlayerResult
import com.spruhs.kick_app.match.core.adapter.primary.EnterResultRequest
import com.spruhs.kick_app.match.core.adapter.primary.PlanMatchRequest
import com.spruhs.kick_app.match.core.adapter.primary.PlayerMatchResult
import com.spruhs.kick_app.match.core.application.AddRegistrationCommand
import com.spruhs.kick_app.match.core.application.CancelMatchCommand
import com.spruhs.kick_app.match.core.application.ChangePlaygroundCommand
import com.spruhs.kick_app.match.core.application.EnterResultCommand
import com.spruhs.kick_app.match.core.application.PlanMatchCommand
import com.spruhs.kick_app.match.core.domain.MatchAggregate
import com.spruhs.kick_app.match.core.domain.MaxPlayer
import com.spruhs.kick_app.match.core.domain.MinPlayer
import com.spruhs.kick_app.match.core.domain.PlayerCount
import com.spruhs.kick_app.match.core.domain.Playground
import com.spruhs.kick_app.match.core.domain.RegisteredPlayer
import com.spruhs.kick_app.match.core.domain.RegistrationStatus
import com.spruhs.kick_app.match.core.domain.RegistrationStatusType
import com.spruhs.kick_app.view.core.service.MatchProjection
import java.time.LocalDateTime

class TestMatchBuilder {

    var matchId = "testMatchId"
    var groupId = "testGroupId"
    var start: LocalDateTime = LocalDateTime.now()
    var isCanceled = false
    val playground = "testPlayground"
    val maxPlayers = 8
    val minPlayers = 4
    val cadre = listOf(
        RegisteredPlayer(UserId("player 1"), LocalDateTime.now(), RegistrationStatus.Registered),
        RegisteredPlayer(UserId("player 2"), LocalDateTime.now(), RegistrationStatus.Registered),
        )
    val waitingBench = listOf(
        RegisteredPlayer(UserId("player 3"), LocalDateTime.now(), RegistrationStatus.Registered),
        RegisteredPlayer(UserId("player 4"), LocalDateTime.now(), RegistrationStatus.Registered),
    )
    val deregistered = listOf(
        RegisteredPlayer(UserId("player 5"), LocalDateTime.now(), RegistrationStatus.Deregistered),
        RegisteredPlayer(UserId("player 6"), LocalDateTime.now(), RegistrationStatus.Deregistered),
    )
    val participatingPlayers = listOf(
        ParticipatingPlayer(UserId("player 1"), PlayerResult.WIN, MatchTeam.A),
        ParticipatingPlayer(UserId("player 2"), PlayerResult.LOSS,MatchTeam.B),
        ParticipatingPlayer(UserId("player 3"), PlayerResult.WIN,MatchTeam.A),
        ParticipatingPlayer(UserId("player 4"), PlayerResult.LOSS,MatchTeam.B),
    )

    fun withStart(start: LocalDateTime) = apply { this.start = start }
    fun withGroupId(groupId: String) = apply { this.groupId = groupId }
    fun withId(matchId: String) = apply { this.matchId = matchId }
    fun withIsCanceled(isCanceled: Boolean) = apply { this.isCanceled = isCanceled }


    fun build(): MatchAggregate {
        return MatchAggregate(matchId).also { match ->
            match.groupId = GroupId(this.groupId)
            match.start = this.start
            match.isCanceled = this.isCanceled
            match.playground = Playground(this.playground)
            match.playerCount = PlayerCount(MinPlayer(minPlayers), MaxPlayer(maxPlayers))
            this.cadre.forEach { player ->
                match.cadre.add(player)
            }
            this.waitingBench.forEach { player ->
                match.waitingBench.add(player)
            }
            this.deregistered.forEach { player ->
                match.deregistered.add(player)
            }
        }
    }

    fun toPlanMatchRequest(): PlanMatchRequest {
        return PlanMatchRequest(
            groupId = this.groupId,
            start = this.start,
            playground = this.playground,
            maxPlayer = this.maxPlayers,
            minPlayer = this.minPlayers
        )
    }

    fun toPlanMatchCommand(requestingUserId: UserId): PlanMatchCommand {
        return PlanMatchCommand(
            requesterId = requestingUserId,
            groupId = GroupId(this.groupId),
            start = this.start,
            playground = Playground(this.playground),
            playerCount = PlayerCount(MinPlayer(this.minPlayers), MaxPlayer(this.maxPlayers))
        )
    }

    fun toEnterResultRequest(): EnterResultRequest {
        return EnterResultRequest(
            players = participatingPlayers.map { PlayerMatchResult(it.userId.value, it.playerResult.name, it.team.name) }
        )
    }

    fun toEnterResultCommand(userId: UserId): EnterResultCommand {
        return EnterResultCommand(
            userId = userId,
            matchId = MatchId(this.matchId),
            players = this.participatingPlayers.map { player ->
                ParticipatingPlayer(
                    userId = player.userId,
                    playerResult = player.playerResult,
                    team = player.team
                )
            }
        )
    }

    fun toCancelMatchCommand(userId: UserId): CancelMatchCommand {
        return CancelMatchCommand(
            userId = userId,
            matchId = MatchId(this.matchId)
        )
    }

    fun toChangePlaygroundCommand(userId: UserId, playground: Playground): ChangePlaygroundCommand {
        return ChangePlaygroundCommand(
            userId = userId,
            matchId = MatchId(this.matchId),
            playground = playground
        )
    }

    fun toAddRegistrationCommand(updatingUser: UserId, updatedUser: UserId, status: RegistrationStatusType): AddRegistrationCommand {
        return AddRegistrationCommand(
            updatingUser = updatingUser,
            updatedUser = updatedUser,
            matchId = MatchId(this.matchId),
            status = status
        )
    }

    fun toProjection() = MatchProjection(
        id = MatchId(matchId),
        groupId = GroupId(groupId),
        start = start,
        playground = playground,
        isCanceled = isCanceled,
        maxPlayer = maxPlayers,
        minPlayer = minPlayers,
        cadrePlayers = cadre.map { it.userId }.toSet(),
        waitingBenchPlayers = waitingBench.map { it.userId }.toSet(),
        deregisteredPlayers = deregistered.map { it.userId }.toSet(),
        result = participatingPlayers
    )
}