package com.spruhs.kick_app.view

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.view.core.service.PlayerStatisticProjection

class TestStatisticBuilder {

    var id = "stat-123"
    var groupId = GroupId("group-456")
    var userId = UserId("user-789")
    var totalMatches = 10
    var wins = 5
    var losses = 3
    var draws = 2

    fun build(): PlayerStatisticProjection {
        return PlayerStatisticProjection(
            id = id,
            groupId = groupId,
            userId = userId,
            totalMatches = totalMatches,
            wins = wins,
            losses = losses,
            draws = draws
        )
    }
}