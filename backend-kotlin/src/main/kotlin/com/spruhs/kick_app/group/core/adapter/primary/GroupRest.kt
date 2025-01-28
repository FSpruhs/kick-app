package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.group.core.application.CreateGroupCommand
import com.spruhs.kick_app.group.core.application.GroupUseCases
import com.spruhs.kick_app.group.core.application.InviteUserCommand
import com.spruhs.kick_app.group.core.application.InviteUserResponseCommand
import com.spruhs.kick_app.group.core.domain.Group
import com.spruhs.kick_app.group.core.domain.GroupNotFoundException
import com.spruhs.kick_app.group.core.domain.Name
import com.spruhs.kick_app.group.core.domain.UserAlreadyInGroupException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// TODO leave group, get group details, update player, remove player

@RestController
@RequestMapping("/api/v1/group")
class GroupRest(val groupUseCases: GroupUseCases, val jwtParser: JWTParser) {

    @GetMapping("/player/{userId}")
    fun getGroups(
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): List<GroupMessage> {
        require(userId == jwtParser.getUserId(jwt)) { UserNotAuthorizedException(UserId(userId)) }

        return groupUseCases.getGroupsByPlayer(UserId(userId)).map { it.toMessage() }
    }

    @PostMapping
    fun createGroup(@AuthenticationPrincipal jwt: Jwt, @RequestBody request: CreateGroupRequest) {
        val userId = jwtParser.getUserId(jwt)
        groupUseCases.create(CreateGroupCommand(UserId(userId), Name(request.name)))
    }

    @PostMapping("{groupId}/invited-users/{userId}")
    fun inviteUser(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: String,
        @PathVariable userId: String
    ) {
        val inviterId = jwtParser.getUserId(jwt)
        groupUseCases.inviteUser(
            InviteUserCommand(
                inviterId = UserId(inviterId),
                inviteeId = UserId(userId),
                groupId = GroupId(groupId)
            )
        )
    }

    @PutMapping("/invited-users")
    fun invitedUserResponse(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: InviteUserResponse
    ) {
        val inviterId = jwtParser.getUserId(jwt)
        require(request.userId == inviterId) { UserNotAuthorizedException(UserId(request.userId)) }
        groupUseCases.inviteUserResponse(request.toCommand())
    }
}

@ControllerAdvice
class GroupExceptionHandler {

    @ExceptionHandler
    fun handleGroupNotFoundException(e: GroupNotFoundException) =
        ResponseEntity(e.message, HttpStatus.BAD_REQUEST)

    @ExceptionHandler
    fun handleUserAlreadyInGroupException(e: UserAlreadyInGroupException) =
        ResponseEntity(e.message, HttpStatus.BAD_REQUEST)

}

data class CreateGroupRequest(
    val name: String,
)

data class InviteUserResponse(
    val groupId: String,
    val userId: String,
    val response: Boolean,
)

data class GroupMessage(
    val id: String,
    val name: String,
)

private fun Group.toMessage() = GroupMessage(
    id = id.value,
    name = name.value
)

private fun InviteUserResponse.toCommand() = InviteUserResponseCommand(
    userId = UserId(this.userId),
    groupId = GroupId(this.groupId),
    response = this.response
)