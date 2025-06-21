package com.spruhs.kick_app.viewservice.core.controller.rest

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.Result
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.viewservice.core.service.MatchProjection
import com.spruhs.kick_app.viewservice.core.service.MatchService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/match")
class MatchViewRestController(
    private val matchService: MatchService,
    private val jwtParser: JWTParser
) {

    @GetMapping("/{matchId}")
    suspend fun getMatch(
        @PathVariable matchId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): MatchMessage {
        val match = matchService.getMatch(MatchId(matchId), jwtParser.getUserId(jwt))
        return match.toMessage()
    }

    @GetMapping("/group/{groupId}")
    suspend fun getMatchesByGroup(
        @PathVariable groupId: String,
        @RequestParam after: LocalDateTime? = null,
        @RequestParam before: LocalDateTime? = null,
        @RequestParam limit: Int? = null,
        @AuthenticationPrincipal jwt: Jwt
    ): List<MatchMessage> {
        val matches = matchService.getMatchesByGroup(
            GroupId(groupId),
            jwtParser.getUserId(jwt),
            after,
            before,
            limit)
        return matches.map { it.toMessage() }
    }

    @GetMapping("player/{playerId}")
    suspend fun getPlayerMatches(
        @PathVariable playerId: String,
        @RequestParam after: LocalDateTime? = null,
        @AuthenticationPrincipal jwt: Jwt
    ): List<MatchMessage> {
        require(playerId == jwtParser.getUserId(jwt).value) { throw UserNotAuthorizedException(UserId(playerId)) }
        val matches =  matchService.getPlayerMatches(UserId(playerId), after)
        return matches.map { it.toMessage() }
    }

}

private fun MatchProjection.toMessage() = MatchMessage(
    id = this.id.value,
    groupId = this.groupId.value,
    start = this.start,
    playground = this.playground,
    maxPlayer = this.maxPlayer,
    minPlayer = this.minPlayer,
    isCanceled = this.isCanceled,
    cadrePlayers = this.cadrePlayers.map { it.value }.toSet(),
    deregisteredPlayers = this.deregisteredPlayers.map { it.value }.toSet(),
    waitingBenchPlayers = this.waitingBenchPlayers.map { it.value }.toSet(),
    teamA = this.teamA.map { it.value }.toSet(),
    teamB = this.teamB.map { it.value }.toSet(),
    result = this.result
)

data class MatchMessage(
    val id: String,
    val groupId: String,
    val start: LocalDateTime,
    val playground: String? = null,
    val maxPlayer: Int,
    val minPlayer: Int,
    val isCanceled: Boolean,
    val cadrePlayers: Set<String>,
    val deregisteredPlayers: Set<String>,
    val waitingBenchPlayers: Set<String>,
    val teamA: Set<String>,
    val teamB: Set<String>,
    val result: Result?
)