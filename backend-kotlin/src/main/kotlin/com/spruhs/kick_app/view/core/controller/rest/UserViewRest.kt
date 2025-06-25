package com.spruhs.kick_app.view.core.controller.rest

import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.view.core.service.UserProjection
import com.spruhs.kick_app.view.core.service.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/user")
class UserViewRestController(
    private val jwtParser: JWTParser,
    private val userService: UserService,
) {

    @GetMapping("/{id}")
    suspend fun getUser(
        @PathVariable id: String,
        @AuthenticationPrincipal jwt: Jwt
    ): UserMessage {
        require(id == jwtParser.getUserId(jwt).value) { throw UserNotAuthorizedException(UserId(id)) }
        return userService.getUser(UserId(id)).toMessage()
    }

}

data class UserMessage(
    val id: String,
    val nickName: String,
    val email: String,
    val imageId: String? = null,
    val groups: List<GroupInfoMessage> = emptyList(),
)

data class GroupInfoMessage(
    val id: String,
    val name: String,
    val userStatus: PlayerStatusType,
    val userRole: PlayerRole,
    val lastMatch: LocalDateTime? = null,
)

private fun UserProjection.toMessage() = UserMessage(
    id = this.id.value,
    nickName = this.nickName,
    email = this.email,
    imageId = this.userImageId?.value,
    groups = this.groups.map { group ->
        GroupInfoMessage(
            id = group.id.value,
            name = group.name,
            userStatus = group.userStatus,
            userRole = group.userRole,
            lastMatch = group.lastMatch
        )
    }
)