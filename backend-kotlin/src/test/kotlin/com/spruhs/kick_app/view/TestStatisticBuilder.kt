package com.spruhs.kick_app.view

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId
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

    fun withUserId(userId: UserId) = apply { this.userId = userId }
    fun withGroupId(groupId: GroupId) = apply { this.groupId = groupId }
    fun withTotalMatches(totalMatches: Int) = apply { this.totalMatches = totalMatches }
    fun withWins(wins: Int) = apply { this.wins = wins }
    fun withLosses(losses: Int) = apply { this.losses = losses }
    fun withDraws(draws: Int) = apply { this.draws = draws }
}