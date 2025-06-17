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
    private val matchCommandPort: MatchCommandPort,
    private val matchQueryPort: MatchQueryPort,
    private val jwtParser: JWTParser
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun planMatch(
        @RequestBody request: PlanMatchRequest,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        matchCommandPort.plan(request.toCommand(jwtParser.getUserId(jwt)))
    }

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

    @GetMapping("/{matchId}")
    suspend fun getMatch(
        @PathVariable matchId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): MatchMessage {
        val (match, groupNameList) = matchQueryPort
            .getMatch(MatchId(matchId), jwtParser.getUserId(jwt))
        return match.toMessage(groupNameList)
    }

    @GetMapping("/group/{groupId}")
    suspend fun getMatchPreviews(
        @PathVariable groupId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): List<MatchPreviewMessage> {
        return matchQueryPort.getMatchesByGroup(GroupId(groupId), jwtParser.getUserId(jwt))
            .map { it.toPreviewMessage() }
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

data class MatchPreviewMessage(
    val id: String,
    val isCanceled: Boolean,
    val start: LocalDateTime,
)

data class MatchMessage(
    val id: String,
    val groupId: String,
    val start: LocalDateTime,
    val playground: String?,
    val maxPlayer: Int,
    val minPlayer: Int,
    val isCanceled: Boolean,
    val cadrePlayers: Set<PlayerMessage>,
    val deregisteredPlayers: Set<PlayerMessage>,
    val waitingBenchPlayers: Set<PlayerMessage>,
    val teamA: Set<PlayerMessage>,
    val teamB: Set<PlayerMessage>,
    val result: Result?
)

data class PlayerMessage(
    val id: String,
    val nickname: String,
)

private fun PlanMatchRequest.toCommand(requestingUserId: UserId) = PlanMatchCommand(
    requesterId = requestingUserId,
    groupId = GroupId(groupId),
    start = start,
    playground = Playground(playground),
    playerCount = PlayerCount(MinPlayer(minPlayer), MaxPlayer(maxPlayer))
)

private fun MatchProjection.toMessage(groupNameList: Map<UserId, String>) = MatchMessage(
    id = this.id.value,
    groupId = this.groupId.value,
    start = this.start,
    playground = this.playground?.value,
    maxPlayer = this.playerCount.maxPlayer.value,
    minPlayer = this.playerCount.minPlayer.value,
    isCanceled = this.isCanceled,
    cadrePlayers = this.cadrePlayers.map { PlayerMessage(it.value, groupNameList[it] ?: "") }.toSet(),
    deregisteredPlayers = this.deregisteredPlayers.map { PlayerMessage(it.value, groupNameList[it] ?: "") }.toSet(),
    waitingBenchPlayers = this.waitingBenchPlayers.map { PlayerMessage(it.value, groupNameList[it] ?: "") }.toSet(),
    teamA = this.teamA.map { PlayerMessage(it.value, groupNameList[it] ?: "") }.toSet(),
    teamB = this.teamB.map { PlayerMessage(it.value, groupNameList[it] ?: "") }.toSet(),
    result = this.result
)

private fun MatchProjection.toPreviewMessage() = MatchPreviewMessage(
    id = this.id.value,
    isCanceled = this.isCanceled,
    start = this.start
)