package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.group.core.application.*
import com.spruhs.kick_app.group.core.domain.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/group")
class GroupRest(
    private val groupUseCases: GroupUseCases,
    private val jwtParser: JWTParser
) {

    @PutMapping("/{groupId}/players/{userId}")
    suspend fun updatePlayer(
        @PathVariable groupId: String,
        @PathVariable userId: String,
        @RequestParam status: String?,
        @RequestParam role: String?,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        groupUseCases.updatePlayer(
            UpdatePlayerCommand(
                userId = UserId(userId),
                updatingUserId = UserId(jwtParser.getUserId(jwt)),
                groupId = GroupId(groupId),
                newStatus = status.takeIf { !it.isNullOrBlank() }?.let { PlayerStatusType.valueOf(it) },
                newRole = role.takeIf { !it.isNullOrBlank() }?.let { PlayerRole.valueOf(it) }
            )
        )
    }

    @GetMapping("/{groupId}")
    suspend fun getGroup(
        @PathVariable groupId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): GroupDetail =
        groupUseCases.getGroupDetails(GroupId(groupId), UserId(jwtParser.getUserId(jwt)))

    @GetMapping("/player/{userId}")
    suspend fun getGroups(
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): List<GroupMessage> {
        require(userId == jwtParser.getUserId(jwt)) { throw UserNotAuthorizedException(UserId(userId)) }

        return groupUseCases.getGroupsByPlayer(UserId(userId)).map { it.toMessage() }
    }

    @PostMapping
    suspend fun createGroup(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateGroupRequest
    ) {
        groupUseCases.create(CreateGroupCommand(UserId(jwtParser.getUserId(jwt)), Name(request.name)))
    }

    @PostMapping("{groupId}/invited-users/{userId}")
    suspend fun inviteUser(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: String,
        @PathVariable userId: String
    ) {
        groupUseCases.inviteUser(
            InviteUserCommand(
                inviterId = UserId(jwtParser.getUserId(jwt)),
                inviteeId = UserId(userId),
                groupId = GroupId(groupId)
            )
        )
    }

    @PutMapping("/invited-users")
    suspend fun invitedUserResponse(
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
