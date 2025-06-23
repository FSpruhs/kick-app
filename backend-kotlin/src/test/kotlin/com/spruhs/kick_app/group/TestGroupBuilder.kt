package com.spruhs.kick_app.group

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.UserId

import com.spruhs.kick_app.group.core.domain.PlayerStatus
import com.spruhs.kick_app.view.core.persistence.GroupDocument
import com.spruhs.kick_app.view.core.persistence.PlayerDocument
import com.spruhs.kick_app.view.core.service.GroupProjection
import com.spruhs.kick_app.view.core.service.PlayerProjection

class TestGroupBuilder() {
    private var groupId = "groupId"
    private var groupName = "groupName"
    private var players = listOf<PlayerProjection>()

    fun buildDocument(): GroupDocument {
        return GroupDocument(
            id = groupId,
            name = groupName,
            players = players.map { PlayerDocument(it.id.value, it.status.name, it.role.name, null, "") }
        )
    }

    fun buildProjection(): GroupProjection {
        return GroupProjection(
            id = GroupId(groupId),
            name = groupName,
            players = players
        )
    }

    fun withPlayer(playerId: String, status: PlayerStatus, role: PlayerRole) =
        also { players += PlayerProjection(UserId(playerId), status.type(), role, null, "") }

    fun withName(name: String) = also { this.groupName = name }

    fun withId(id: String) = also { this.groupId = id }

}