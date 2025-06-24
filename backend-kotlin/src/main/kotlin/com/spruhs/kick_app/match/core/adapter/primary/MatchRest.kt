package com.spruhs.kick_app.match.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.Result
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
    private val matchCommandPort: MatchCommandPort,
    private val jwtParser: JWTParser
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun planMatch(
        @RequestBody request: PlanMatchRequest,
        @AuthenticationPrincipal jwt: Jwt
    ) = matchCommandPort
        .plan(request.toCommand(jwtParser.getUserId(jwt)))
        .aggregateId

    @DeleteMapping("/{matchId}")
    suspend fun cancelMatch(
        @PathVariable matchId: String,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        matchCommandPort.cancelMatch(
            CancelMatchCommand(
                userId = jwtParser.getUserId(jwt),
                matchId = MatchId(matchId)
            )
        )
    }

    @PutMapping("/{matchId}/playground")
    suspend fun changePlayground(
        @PathVariable matchId: String,
        @RequestParam playground: String,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        matchCommandPort.changePlayground(
            ChangePlaygroundCommand(
                userId = jwtParser.getUserId(jwt),
                matchId = MatchId(matchId),
                playground = Playground(playground)
            )
        )
    }

    @PutMapping("/{matchId}/players/{userId}")
    suspend fun updatePlayerRegistration(
        @PathVariable matchId: String,
        @PathVariable userId: String,
        @RequestParam status: String,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        matchCommandPort.addRegistration(
            AddRegistrationCommand(
                updatedUser = UserId(userId),
                updatingUser = jwtParser.getUserId(jwt),
                matchId = MatchId(matchId),
                status = RegistrationStatusType.valueOf(status),
            )
        )
    }

    @PostMapping("/{matchId}/result")
    suspend fun addResult(
        @PathVariable matchId: String,
        @RequestBody request: EnterResultRequest,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        matchCommandPort.enterResult(
            EnterResultCommand(
                userId = jwtParser.getUserId(jwt),
                matchId = MatchId(matchId),
                result = request.result,
                teamA = request.teamA.map { UserId(it) }.toSet(),
                teamB = request.teamB.map { UserId(it) }.toSet()
            )
        )
    }
}

data class EnterResultRequest(
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

private fun PlanMatchRequest.toCommand(requestingUserId: UserId) = PlanMatchCommand(
    requesterId = requestingUserId,
    groupId = GroupId(groupId),
    start = start,
    playground = Playground(playground),
    playerCount = PlayerCount(MinPlayer(minPlayer), MaxPlayer(maxPlayer))
)