package com.spruhs.kick_app.view.core.controller.rest

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.helper.JWTParser
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.aop.OwnerOnly
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.view.core.service.MatchFilter
import com.spruhs.kick_app.view.core.service.MatchProjection
import com.spruhs.kick_app.view.core.service.MatchService
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
    ): MatchMessage = matchService.getMatch(MatchId(matchId), jwtParser.getUserId(jwt)).toMessage()

    @GetMapping("/group/{groupId}")
    suspend fun getMatchesByGroup(
        @PathVariable groupId: String,
        @RequestParam after: LocalDateTime? = null,
        @RequestParam before: LocalDateTime? = null,
        @RequestParam limit: Int? = null,
        @AuthenticationPrincipal jwt: Jwt
    ): List<MatchMessage> =
        matchService.getMatchesByGroup(
            GroupId(groupId),
            jwtParser.getUserId(jwt),
            MatchFilter(
                after = after,
                before = before,
                limit = limit
            )
        ).map { it.toMessage() }

    @GetMapping("player/{playerId}")
    @OwnerOnly(pathParam = "playerId")
    suspend fun getPlayerMatches(
        @PathVariable playerId: String,
        @RequestParam after: LocalDateTime? = null,
        @AuthenticationPrincipal jwt: Jwt
    ): List<MatchMessage> = matchService.getPlayerMatches(UserId(playerId), after)
        .map { it.toMessage() }
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
    result = this.result.map { PlayerResultMessage(
        userId = it.userId.value,
        result = it.playerResult.name,
        team = it.team.name
    ) }
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
    val result: List<PlayerResultMessage>,
)

data class PlayerResultMessage(
    val userId: String,
    val result: String,
    val team: String
)