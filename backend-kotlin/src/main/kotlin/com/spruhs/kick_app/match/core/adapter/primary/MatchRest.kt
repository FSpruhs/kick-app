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

    @DeleteMapping("/{matchId}/players/{userId}")
    fun cancelPlayer(
        @PathVariable matchId: String,
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        matchUseCases.cancelPlayer(
            CancelPlayerCommand(
                cancelingUserId = UserId(userId),
                userId = UserId(jwtParser.getUserId(jwt)),
                matchId = MatchId(matchId)
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

    @PutMapping("/{matchId}/registeredPlayers/{registrationStatus}")
    fun addRegistration(
        @PathVariable matchId: String,
        @PathVariable registrationStatus: String,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        matchUseCases.addRegistration(
            AddRegistrationCommand(
                userId = UserId(jwtParser.getUserId(jwt)),
                matchId = MatchId(matchId),
                registrationStatus = RegistrationStatus.valueOf(registrationStatus)
            )
        )
    }
}

data class PlanMatchRequest(
    val groupId: String,
    val start: LocalDateTime,
    val playground: String,
    val maxPlayer: Int,
    val minPlayer: Int
)

fun PlanMatchRequest.toCommand() = PlanMatchCommand(
    groupId = GroupId(groupId),
    start = start,
    playground = Playground(playground),
    playerCount = PlayerCount(MinPlayer(minPlayer), MaxPlayer(maxPlayer))
)