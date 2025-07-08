package com.spruhs.kick_app.match.core.adapter.primary

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.helper.JWTParser
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.types.MatchNotFoundException
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.match.api.MatchTeam
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.api.PlayerResult
import com.spruhs.kick_app.match.core.application.*
import com.spruhs.kick_app.match.core.domain.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
                players = request.players.map { it.toParticipatingPlayer() },
            )
        )
    }
}

@ControllerAdvice
class MatchExceptionHandler {

    @ExceptionHandler
    fun handleMatchNotFoundException(ex: MatchNotFoundException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)

    @ExceptionHandler
    fun handleMatchStartTimeException(ex: MatchStartTimeException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)

    @ExceptionHandler
    fun handleMatchCanceledException(ex: MatchCanceledException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)
}

data class EnterResultRequest(
    val players: List<PlayerMatchResult>,
)

data class PlayerMatchResult(
    val userId: String,
    val result: String,
    val team: String
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

private fun PlayerMatchResult.toParticipatingPlayer() = ParticipatingPlayer(
    userId = UserId(userId),
    team = MatchTeam.valueOf(team),
    playerResult = PlayerResult.valueOf(result)
)