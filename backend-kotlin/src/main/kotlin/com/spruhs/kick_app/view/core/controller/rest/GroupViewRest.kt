package com.spruhs.kick_app.view.core.controller.rest

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.view.core.service.GroupNameListEntry
import com.spruhs.kick_app.view.core.service.GroupProjection
import com.spruhs.kick_app.view.core.service.GroupService
import com.spruhs.kick_app.view.core.service.PlayerProjection
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/group")

class GroupViewRestController(
    private val jwtParser: JWTParser,
    private val groupService: GroupService,
) {
    @GetMapping("/{groupId}")
    suspend fun getGroup(
        @PathVariable groupId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): GroupMessage =
        groupService.getGroup(GroupId(groupId), jwtParser.getUserId(jwt)).toMessage()

    @GetMapping("{groupId}/player/{userId}")
    suspend fun getGroupPlayer(
        @PathVariable groupId: String,
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): GroupPlayerMessage =
        groupService.getPlayer(GroupId(groupId), UserId(userId), jwtParser.getUserId(jwt))
            .toMessage()

    @GetMapping("/{groupId}/name-list")
    suspend fun getGroupNameList(
        @PathVariable groupId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): List<GroupNameEntryMessage> =
        groupService.getGroupNameList(GroupId(groupId), jwtParser.getUserId(jwt))
            .map { it.toMessage() }

}

data class GroupMessage(
    val groupId: String,
    val name: String,
    val players: List<GroupPlayerMessage>
)

data class GroupPlayerMessage(
    val userId: String,
    val role: String,
    val status: String,
    val avatarUrl: String? = null,
    val email: String,
)

data class GroupNameEntryMessage(
    val userId: String,
    val name: String,
)

private fun GroupNameListEntry.toMessage(): GroupNameEntryMessage = GroupNameEntryMessage(
    userId = userId.value,
    name = name,
)

private fun GroupProjection.toMessage(): GroupMessage = GroupMessage(
    groupId = id.value,
    name = name,
    players = players.map { player -> player.toMessage() }
)


private fun PlayerProjection.toMessage(): GroupPlayerMessage = GroupPlayerMessage(
    userId = id.value,
    role = role.name,
    status = status.name,
    email = email,
)