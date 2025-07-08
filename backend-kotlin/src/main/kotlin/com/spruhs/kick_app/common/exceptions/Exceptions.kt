package com.spruhs.kick_app.common.exceptions

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.types.MessageId
import com.spruhs.kick_app.common.types.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

data class UserNotAuthorizedException(val userId: UserId) : RuntimeException("User not authorized: $userId")
data class MessageNotFoundException(val messageId: MessageId) : RuntimeException("Message not found: $messageId")
data class UserNotFoundException(val userId: UserId) : RuntimeException("User not found: $userId")
data class PlayerNotFoundException(val userId: UserId) : RuntimeException("Player not found with id: ${userId.value}")
data class GroupNotFoundException(val groupId: GroupId) : RuntimeException("Group not found with id: ${groupId.value}")
data class MatchNotFoundException(val matchId: MatchId) : RuntimeException("Match not found with id: ${matchId.value}")

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler
    fun handleIllegalArgumentException(ex: IllegalArgumentException) =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.message)
}