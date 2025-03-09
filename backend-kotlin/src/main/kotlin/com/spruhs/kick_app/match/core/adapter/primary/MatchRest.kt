package com.spruhs.kick_app.match.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.match.core.application.*
import com.spruhs.kick_app.match.core.domain.*
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/match")
class MatchRestController(
    private val matchUseCases: MatchUseCases,
    private val jwtParser: JWTParser
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun planMatch(@RequestBody request: PlanMatchRequest) {
        matchUseCases.plan(request.toCommand())
    }

    @GetMapping("/{matchId}")
    fun getMatch(
        @PathVariable matchId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): MatchMessage {
        return matchUseCases.getMatch(MatchId(matchId), UserId(jwtParser.getUserId(jwt))).toMessage()
    }

    @PutMapping("/{matchId}/players/{userId}")
    fun updatePlayerRegistration(
        @PathVariable matchId: String,
        @PathVariable userId: String,
        @RequestParam status: String,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        matchUseCases.updatePlayerRegistration(
            UpdatePlayerRegistrationCommand(
                updatedUser = UserId(userId),
                updatingUser = UserId(jwtParser.getUserId(jwt)),
                matchId = MatchId(matchId),
                status = RegistrationStatus.valueOf(status),
            )
        )
    }

    @DeleteMapping("/{matchId}")
    fun cancelMatch(
        @PathVariable matchId: String,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        matchUseCases.cancel(
            CancelMatchCommand(
                userId = UserId(jwtParser.getUserId(jwt)),
                matchId = MatchId(matchId)
            )
        )
    }

    @PostMapping("/{matchId}/result")
    fun addResult(
        @PathVariable matchId: String,
        @RequestBody request: AddResultRequest,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        matchUseCases.addResult(
            AddResultCommand(
                userId = UserId(jwtParser.getUserId(jwt)),
                matchId = MatchId(matchId),
                result = request.result,
                teamA = request.teamA.map { UserId(it) }.toSet(),
                teamB = request.teamB.map { UserId(it) }.toSet()
            )
        )
    }
}

data class AddResultRequest(
    val teamA: List<String>,
    val teamB: List<String>,
    val result: Result
)

data class PlanMatchRequest(
    val groupId: String,
    val start: LocalDateTime,
    val playground: String,
    val maxPlayer: Int,
    val minPlayer: Int
)

data class MatchMessage(
    val matchId: String,
    val groupId: String,
    val start: LocalDateTime,
    val playground: String,
    val maxPlayer: Int,
    val minPlayer: Int,
    val acceptedPlayers: List<String>,
    val deregisteredPlayers: List<String>,
    val waitingBenchPlayers: List<String>,
    val teamA: List<String>,
    val teamB: List<String>,
    val result: String?
)

fun Match.toMessage() = MatchMessage(
    matchId = this.id.value,
    groupId = this.groupId.value,
    start = this.start,
    playground = this.playground.value,
    maxPlayer = this.playerCount.maxPlayer.value,
    minPlayer = this.playerCount.minPlayer.value,
    acceptedPlayers = listOf(),
    deregisteredPlayers = listOf(),
    waitingBenchPlayers = listOf(),
    teamA = this.participatingPlayers.filter { it.team == Team.A }.map { it.userId.value },
    teamB = this.participatingPlayers.filter { it.team == Team.A }.map { it.userId.value },
    result = this.result?.name
)

fun PlanMatchRequest.toCommand() = PlanMatchCommand(
    groupId = GroupId(groupId),
    start = start,
    playground = Playground(playground),
    playerCount = PlayerCount(MinPlayer(minPlayer), MaxPlayer(maxPlayer))
)