package com.spruhs.kick_app.match.api

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId

fun interface MatchApi {
    suspend fun findPlanningMatchIds(groupId: GroupId): List<MatchId>
}
