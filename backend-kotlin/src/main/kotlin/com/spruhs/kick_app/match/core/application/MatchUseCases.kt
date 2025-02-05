package com.spruhs.kick_app.match.core.application

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.match.core.adapter.secondary.MatchPersistenceAdapter
import com.spruhs.kick_app.match.core.domain.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MatchUseCases(val matchPersistenceAdapter: MatchPersistenceAdapter) {
    fun plan(command: PlanMatchCommand) {
        planMatch(
            groupId = command.groupId,
            start = command.start,
            playground = command.playground,
            playerCount = command.playerCount
        ).apply {
            matchPersistenceAdapter.save(this)
        }
    }
}

data class PlanMatchCommand(
    val groupId: GroupId,
    val start: LocalDateTime,
    val playground: Playground,
    val playerCount: PlayerCount,
)