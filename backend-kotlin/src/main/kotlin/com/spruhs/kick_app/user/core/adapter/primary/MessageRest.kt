package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.MessageId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.user.core.application.MarkAsReadCommand
import com.spruhs.kick_app.user.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.domain.Message
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/message")
class MessageRestController(
    private val messageUseCases: MessageUseCases,
    private val jwtParser: JWTParser
) {

    @GetMapping("/user/{userId}")
    fun getMessagesByUser(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable userId: String
    ): List<MessageResponse> {
        require(jwtParser.getUserId(jwt) == userId) { UserNotAuthorizedException(UserId(userId)) }
        return messageUseCases.getByUser(UserId(userId)).map { it.toResponse() }
    }

    @PutMapping("/{messageId}/read")
    fun markMessageAsRead(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable messageId: String
    ) {
        messageUseCases.markAsRead(
            MarkAsReadCommand(
                messageId = MessageId(messageId),
                userId = UserId(jwtParser.getUserId(jwt))
            )
        )
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