package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.exceptions.GroupNotFoundException
import com.spruhs.kick_app.common.exceptions.PlayerNotFoundException
import com.spruhs.kick_app.common.exceptions.UserNotAuthorizedException
import com.spruhs.kick_app.common.helper.JWTParser
import com.spruhs.kick_app.common.types.Email
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.group.core.application.ChangeGroupNameCommand
import com.spruhs.kick_app.group.core.application.CreateGroupCommand
import com.spruhs.kick_app.group.core.application.GroupCommandPort
import com.spruhs.kick_app.group.core.application.InviteUserCommand
import com.spruhs.kick_app.group.core.application.InviteUserResponseCommand
import com.spruhs.kick_app.group.core.application.UpdatePlayerRoleCommand
import com.spruhs.kick_app.group.core.application.UpdatePlayerStatusCommand
import com.spruhs.kick_app.group.core.domain.Name
import com.spruhs.kick_app.group.core.domain.PlayerAlreadyInGroupException
import com.spruhs.kick_app.group.core.domain.PlayerNotInvitedInGroupException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/group")
class GroupRest(
    private val groupCommandPort: GroupCommandPort,
    private val jwtParser: JWTParser,
) {
    @PutMapping("/{groupId}/players/{userId}")
    suspend fun updatePlayer(
        @PathVariable groupId: String,
        @PathVariable userId: String,
        @RequestParam status: String?,
        @RequestParam role: String?,
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        if (!status.isNullOrBlank()) {
            groupCommandPort.updatePlayerStatus(
                UpdatePlayerStatusCommand(
                    userId = UserId(userId),
                    updatingUserId = jwtParser.getUserId(jwt),
                    groupId = GroupId(groupId),
                    newStatus = PlayerStatusType.valueOf(status),
                ),
            )
        }
        if (!role.isNullOrBlank()) {
            groupCommandPort.updatePlayerRole(
                UpdatePlayerRoleCommand(
                    userId = UserId(userId),
                    updatingUserId = jwtParser.getUserId(jwt),
                    groupId = GroupId(groupId),
                    newRole = PlayerRole.valueOf(role),
                ),
            )
        }
    }

    @DeleteMapping("/{groupId}/players/{userId}")
    suspend fun removePlayer(
        @PathVariable groupId: String,
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        groupCommandPort.updatePlayerStatus(
            UpdatePlayerStatusCommand(
                userId = UserId(userId),
                updatingUserId = jwtParser.getUserId(jwt),
                groupId = GroupId(groupId),
                newStatus =
                    if (UserId(userId) == jwtParser.getUserId(jwt)) {
                        PlayerStatusType.LEAVED
                    } else {
                        PlayerStatusType.REMOVED
                    },
            ),
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun createGroup(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateGroupRequest,
    ): String =
        groupCommandPort
            .createGroup(CreateGroupCommand(jwtParser.getUserId(jwt), Name(request.name)))
            .aggregateId

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
                newName = Name(name),
            ),
        )
    }

    @PostMapping("{groupId}/invited-users/{email}")
    suspend fun inviteUser(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: String,
        @PathVariable email: String,
    ) {
        groupCommandPort.inviteUser(
            InviteUserCommand(
                inviterId = jwtParser.getUserId(jwt),
                email = Email(email),
                groupId = GroupId(groupId),
            ),
        )
    }

    @PutMapping("/invited-users")
    suspend fun invitedUserResponse(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: InviteUserResponse,
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
    fun handleGroupNotFoundException(e: GroupNotFoundException) = ResponseEntity(e.message, HttpStatus.NOT_FOUND)

    @ExceptionHandler
    fun handlePlayerAlreadyInGroupException(e: PlayerAlreadyInGroupException) = ResponseEntity(e.message, HttpStatus.BAD_REQUEST)

    @ExceptionHandler
    fun handlePlayerNotFoundException(e: PlayerNotFoundException) = ResponseEntity(e.message, HttpStatus.NOT_FOUND)

    @ExceptionHandler
    fun handlePlayerNotInvitedInGroupException(e: PlayerNotInvitedInGroupException) = ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
}

data class CreateGroupRequest(
    val name: String,
)

data class InviteUserResponse(
    val groupId: String,
    val userId: String,
    val response: Boolean,
)

private fun InviteUserResponse.toCommand() =
    InviteUserResponseCommand(
        userId = UserId(this.userId),
        groupId = GroupId(this.groupId),
        response = this.response,
    )
