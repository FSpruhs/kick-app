package com.spruhs.kick_app.match

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.Result
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.match.core.adapter.primary.EnterResultRequest
import com.spruhs.kick_app.match.core.adapter.primary.PlanMatchRequest
import com.spruhs.kick_app.match.core.application.EnterResultCommand
import com.spruhs.kick_app.match.core.application.PlanMatchCommand
import com.spruhs.kick_app.match.core.domain.MatchAggregate
import com.spruhs.kick_app.match.core.domain.MaxPlayer
import com.spruhs.kick_app.match.core.domain.MinPlayer
import com.spruhs.kick_app.match.core.domain.ParticipatingPlayer
import com.spruhs.kick_app.match.core.domain.PlayerCount
import com.spruhs.kick_app.match.core.domain.Playground
import com.spruhs.kick_app.match.core.domain.RegisteredPlayer
import com.spruhs.kick_app.match.core.domain.RegistrationStatus
import com.spruhs.kick_app.match.core.domain.Team
import java.time.LocalDateTime

class TestMatchBuilder {

    val matchId = "testMatchId"
    val groupId = "testGroupId"
    val start = LocalDateTime.now()
    val isCanceled = false
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
    val result = Result.WINNER_TEAM_A
    val participatingPlayers = listOf(
        ParticipatingPlayer(UserId("player 1"), Team.A),
        ParticipatingPlayer(UserId("player 2"), Team.B),
        ParticipatingPlayer(UserId("player 3"), Team.A),
        ParticipatingPlayer(UserId("player 4"), Team.B),
    )


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
            match.result = this.result
            this.participatingPlayers.forEach { player ->
                match.participatingPlayers.add(player)
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
            teamA = this.participatingPlayers.filter { it.team == Team.A }.map { it.userId.value },
            teamB = this.participatingPlayers.filter { it.team == Team.B }.map { it.userId.value },
            result = this.result
        )
    }

    fun toEnterResultCommand(userId: UserId): EnterResultCommand {
        return EnterResultCommand(
            userId = userId,
            matchId = MatchId(this.matchId),
            result = this.result,
            teamA = this.participatingPlayers.filter { it.team == Team.A }.map { it.userId }.toSet(),
            teamB = this.participatingPlayers.filter { it.team == Team.B }.map { it.userId }.toSet()
        )
    }
}