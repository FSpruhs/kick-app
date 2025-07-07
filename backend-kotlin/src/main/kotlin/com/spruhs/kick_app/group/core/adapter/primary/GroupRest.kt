package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.GroupNotFoundException
import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.PlayerNotFoundException
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
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
    private val groupCommandPort: GroupCommandPort,
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
        if (!status.isNullOrBlank()) {
            groupCommandPort.updatePlayerStatus(
                UpdatePlayerStatusCommand(
                    userId = UserId(userId),
                    updatingUserId = jwtParser.getUserId(jwt),
                    groupId = GroupId(groupId),
                    newStatus = PlayerStatusType.valueOf(status)
                )
            )
        }
        if (!role.isNullOrBlank()) {
            groupCommandPort.updatePlayerRole(
                UpdatePlayerRoleCommand(
                    userId = UserId(userId),
                    updatingUserId = jwtParser.getUserId(jwt),
                    groupId = GroupId(groupId),
                    newRole = PlayerRole.valueOf(role)
                )
            )
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createGroup(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateGroupRequest
    ): String = groupCommandPort
        .createGroup(CreateGroupCommand(jwtParser.getUserId(jwt), Name(request.name))).aggregateId

    @PutMapping("/{groupId}/name")
    suspend fun updateGroupName(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: String,
        @RequestParam name: String,
    ) {
        groupCommandPort.changeGroupName(
            ChangeGroupNameCommand(
                groupId = GroupId(groupId),
                userId = jwtParser.getUserId(jwt),
                newName = Name(name)
            )
        )
    }

    @PostMapping("{groupId}/invited-users/{userId}")
    suspend fun inviteUser(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: String,
        @PathVariable userId: String
    ) {
        groupCommandPort.inviteUser(
            InviteUserCommand(
                inviterId = jwtParser.getUserId(jwt),
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
        require(request.userId == jwtParser.getUserId(jwt).value) {
            throw UserNotAuthorizedException(UserId(request.userId))
        }
        groupCommandPort.inviteUserResponse(request.toCommand())
    }
}

@ControllerAdvice
class GroupExceptionHandler {

    @ExceptionHandler
    fun handleGroupNotFoundException(e: GroupNotFoundException) =
        ResponseEntity(e.message, HttpStatus.NOT_FOUND)

    @ExceptionHandler
    fun handlePlayerAlreadyInGroupException(e: PlayerAlreadyInGroupException) =
        ResponseEntity(e.message, HttpStatus.BAD_REQUEST)

    @ExceptionHandler
    fun handlePlayerNotFoundException(e: PlayerNotFoundException) =
        ResponseEntity(e.message, HttpStatus.NOT_FOUND)

    @ExceptionHandler
    fun handlePlayerNotInvitedInGroupException(e: PlayerNotInvitedInGroupException) =
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

private fun InviteUserResponse.toCommand() = InviteUserResponseCommand(
    userId = UserId(this.userId),
    groupId = GroupId(this.groupId),
    response = this.response
)
