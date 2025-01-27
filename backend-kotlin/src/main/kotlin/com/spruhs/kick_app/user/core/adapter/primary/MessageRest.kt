package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.user.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.domain.Message
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/message")
class MessageRestController(val messageUseCases: MessageUseCases, val jwtParser: JWTParser) {

    @GetMapping("/user/{userId}")
    fun getMessagesByUser(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable userId: String
    ): List<MessageResponse> {
        val tokenUserId = jwtParser.getUserId(jwt)
        if (tokenUserId != userId) {
            throw UserNotAuthorizedException(UserId(userId))
        }
        return messageUseCases.getByUser(UserId(userId)).map { it.toResponse() }
    }

}

data class MessageResponse(
    val id: String,
    val userId: String,
    val text: String,
    val timeStamp: String,
    val isRead: Boolean,
    val variables: Map<String, String>
)

private fun Message.toResponse() = MessageResponse(
    id = id.value,
    userId = user.value,
    text = text,
    timeStamp = timeStamp.toString(),
    isRead = isRead,
    variables = variables
)