package com.spruhs.kick_app.match.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.match.core.application.MatchUseCases
import com.spruhs.kick_app.match.core.application.PlanMatchCommand
import com.spruhs.kick_app.match.core.domain.MaxPlayer
import com.spruhs.kick_app.match.core.domain.MinPlayer
import com.spruhs.kick_app.match.core.domain.PlayerCount
import com.spruhs.kick_app.match.core.domain.Playground
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/match")
class MatchRestController(val matchUseCases: MatchUseCases) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun planMatch(@RequestBody request: PlanMatchRequest) {
        matchUseCases.plan(request.toCommand())
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