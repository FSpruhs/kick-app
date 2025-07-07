package com.spruhs.kick_app.view.core.controller.rest

import com.spruhs.kick_app.common.helper.JWTParser
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.UserNotAuthorizedException
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
    private val userService: UserService,
    private val jwtParser: JWTParser
) {

    @GetMapping("/{userId}")
    suspend fun getUser(
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): UserMessage {
        require(userId == jwtParser.getUserId(jwt).value) {
            throw UserNotAuthorizedException(UserId(userId))
        }
        return userService.getUser(UserId(userId)).toMessage()
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