package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.application.CreateGroupCommand
import com.spruhs.kick_app.group.core.application.GroupUseCases
import com.spruhs.kick_app.group.core.application.InviteUserCommand
import com.spruhs.kick_app.group.core.domain.GroupNotFoundException
import com.spruhs.kick_app.group.core.domain.Name
import com.spruhs.kick_app.group.core.domain.UserAlreadyInGroupException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/group")
class GroupRest(val groupUseCases: GroupUseCases, val jwtParser: JWTParser) {

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