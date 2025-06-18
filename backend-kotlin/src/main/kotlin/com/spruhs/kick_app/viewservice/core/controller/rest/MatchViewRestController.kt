package com.spruhs.kick_app.viewservice.core.controller.rest

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.Result
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.viewservice.core.service.MatchProjection
import com.spruhs.kick_app.viewservice.core.service.MatchService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
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
        val (match, groupNameList) = matchService
            .getMatch(MatchId(matchId), jwtParser.getUserId(jwt))
        return match.toMessage(groupNameList)
    }

    @GetMapping("/group/{groupId}")
    suspend fun getMatchPreviews(
        @PathVariable groupId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): List<MatchPreviewMessage> {
        return matchService.getMatchesByGroup(GroupId(groupId), jwtParser.getUserId(jwt))
            .map { it.toPreviewMessage() }
    }
}

private fun MatchProjection.toMessage(groupNameList: Map<UserId, String>) = MatchMessage(
    id = this.id.value,
    groupId = this.groupId.value,
    start = this.start,
    playground = this.playground,
    maxPlayer = this.maxPlayer,
    minPlayer = this.minPlayer,
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