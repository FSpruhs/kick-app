package com.spruhs.kick_app.group

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.adapter.secondary.GroupDocument
import com.spruhs.kick_app.group.core.adapter.secondary.PlayerDocument
import com.spruhs.kick_app.group.core.domain.GroupProjection
import com.spruhs.kick_app.group.core.domain.Name
import com.spruhs.kick_app.group.core.domain.PlayerProjection
import com.spruhs.kick_app.group.core.domain.PlayerStatus

class TestGroupBuilder() {
    private var groupId = "groupId"
    private var groupName = "groupName"
    private var players = listOf<PlayerProjection>()

    fun buildDocument(): GroupDocument {
        return GroupDocument(
            id = groupId,
            name = groupName,
            players = players.map { PlayerDocument(it.id.value, it.status.name, it.role.name) }
        )
    }

    fun buildProjection(): GroupProjection {
        return GroupProjection(
            id = GroupId(groupId),
            name = Name(groupName),
            players = players
        )
    }

    fun withPlayer(playerId: String, status: PlayerStatus, role: PlayerRole) =
        also { players += PlayerProjection(UserId(playerId), status.type(), role) }

    fun withName(name: String) = also { this.groupName = name }

    fun withId(id: String) = also { this.groupId = id }

}