package com.spruhs.kick_app.view.core.controller.rest

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.view.core.service.GroupProjection
import com.spruhs.kick_app.view.core.service.GroupService
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
}

data class GroupMessage(
    val groupId: String,
    val name: String,
    val members: List<GroupMemberMessage>
)

data class GroupMemberMessage(
    val userId: String,
    val role: String,
    val status: String,
    val avatarUrl: String? = null,
    val email: String,
)

private fun GroupProjection.toMessage(): GroupMessage {
    return GroupMessage(
        groupId = id.value,
        name = name,
        members = players.map { player ->
            GroupMemberMessage(
                userId = player.id.value,
                role = player.role.name,
                status = player.status.name,
                avatarUrl = player.avatarUrl,
                email = player.email,
            )
        }
    )
}