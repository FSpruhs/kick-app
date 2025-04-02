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
    private val groupCommandPort: GroupCommandPort,
    private val groupQueryPort: GroupQueryPort,
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
        if (status != null) {
            groupCommandPort.updatePlayerStatus(UpdatePlayerStatusCommand(
                userId = UserId(userId),
                updatingUserId = UserId(jwtParser.getUserId(jwt)),
                groupId = GroupId(groupId),
                newStatus = PlayerStatusType.valueOf(status)
            ))
        }
        if (role != null) {
            groupCommandPort.updatePlayerRole(UpdatePlayerRoleCommand(
                userId = UserId(userId),
                updatingUserId = UserId(jwtParser.getUserId(jwt)),
                groupId = GroupId(groupId),
                newRole = PlayerRole.valueOf(role)
            ))
        }
    }

    @GetMapping("/{groupId}")
    suspend fun getGroup(
        @PathVariable groupId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): GroupDetail =
        groupQueryPort.getGroupDetails(GroupId(groupId), UserId(jwtParser.getUserId(jwt)))

    @GetMapping("/player/{userId}")
    suspend fun getGroups(
        @PathVariable userId: String,
        @AuthenticationPrincipal jwt: Jwt
    ): List<GroupMessage> {
        require(userId == jwtParser.getUserId(jwt)) { throw UserNotAuthorizedException(UserId(userId)) }

        return groupQueryPort.getGroupsByPlayer(UserId(userId)).map { it.toMessage() }
    }

    @PostMapping
    suspend fun createGroup(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateGroupRequest
    ): GroupMessage {
        return groupCommandPort.createGroup(CreateGroupCommand(UserId(jwtParser.getUserId(jwt)), Name(request.name))).toMessage()
    }

    @PutMapping("/{groupId}/name")
    suspend fun updateGroupName(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable groupId: String,
        @RequestParam name: String,
    ) {
        groupCommandPort.changeGroupName(
            ChangeGroupNameCommand(
                groupId = GroupId(groupId),
                userId = UserId(jwtParser.getUserId(jwt)),
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

data class GroupMessage(
    val id: String,
    val name: String,
)

private fun InviteUserResponse.toCommand() = InviteUserResponseCommand(
    userId = UserId(this.userId),
    groupId = GroupId(this.groupId),
    response = this.response
)

private fun GroupAggregate.toMessage() = GroupMessage(
    id = this.aggregateId,
    name = this.name.value
)

private fun Group.toMessage() = GroupMessage(
    id = this.id.value,
    name = this.name.value
)