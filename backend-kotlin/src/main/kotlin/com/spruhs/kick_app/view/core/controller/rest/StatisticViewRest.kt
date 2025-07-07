package com.spruhs.kick_app.view.core.controller.rest

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.helper.JWTParser
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.view.core.service.PlayerStatisticProjection
import com.spruhs.kick_app.view.core.service.StatisticService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/statistic")
class StatisticViewRestController(
    private val statisticService: StatisticService,
    private val jwtParser: JWTParser
) {

    @GetMapping("/group/{groupId}/player/{userId}")
    suspend fun getPlayerStatistics(
        @PathVariable groupId: String,
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): PlayerStatisticMessage = statisticService.getPlayerStatistics(
        groupId = GroupId(groupId),
        userId = UserId(userId),
        requestingUserId = jwtParser.getUserId(jwt)
    ).toMessage()
}

data class PlayerStatisticMessage(
    val groupId: String,
    val userId: String,
    val totalMatches: Int,
    val wins: Int,
    val losses: Int,
    val draws: Int,
)

private fun PlayerStatisticProjection.toMessage() = PlayerStatisticMessage(
    groupId = this.groupId.value,
    userId = this.userId.value,
    totalMatches = this.totalMatches,
    wins = this.wins,
    losses = this.losses,
    draws = this.draws
)