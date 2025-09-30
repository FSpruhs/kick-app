package com.spruhs.kick_app.view.core.controller.rest

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.helper.JWTParser
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.exceptions.UserNotAuthorizedException
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
        @RequestParam userId: String? = null,
        @RequestParam limit: Int? = null,
        @AuthenticationPrincipal jwt: Jwt
    ): List<MatchMessage> =
        matchService.getMatchesByGroup(
            GroupId(groupId),
            jwtParser.getUserId(jwt),
            MatchFilter(
                after = after,
                before = before,
                userId = userId?.let { UserId(it) },
                limit = limit
            )
        ).map { it.toMessage() }

    @GetMapping("player/{playerId}")
    suspend fun getPlayerMatches(
        @PathVariable playerId: String,
        @RequestParam after: LocalDateTime? = null,
        @AuthenticationPrincipal jwt: Jwt
    ): List<MatchMessage> {
        require(playerId == jwtParser.getUserId(jwt).value) {
            throw UserNotAuthorizedException(UserId(playerId))
        }
        return matchService.getPlayerMatches(UserId(playerId), after)
        .map { it.toMessage() }
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
    cadrePlayers = this.cadrePlayers.map { RegisteredPlayerInfoMessage(it.userId.value, it.guestOf?.value) }.toSet(),
    deregisteredPlayers = this.deregisteredPlayers.map { RegisteredPlayerInfoMessage(it.userId.value, it.guestOf?.value) }.toSet(),
    waitingBenchPlayers = this.waitingBenchPlayers.map { RegisteredPlayerInfoMessage(it.userId.value, it.guestOf?.value) }.toSet(),
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
    val cadrePlayers: Set<RegisteredPlayerInfoMessage>,
    val deregisteredPlayers: Set<RegisteredPlayerInfoMessage>,
    val waitingBenchPlayers: Set<RegisteredPlayerInfoMessage>,
    val result: List<PlayerResultMessage>,
)

data class RegisteredPlayerInfoMessage(val userId: String, val guestOf: String?)

data class PlayerResultMessage(
    val userId: String,
    val result: String,
    val team: String
)