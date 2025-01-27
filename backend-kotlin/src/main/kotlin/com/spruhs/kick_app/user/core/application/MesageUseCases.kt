package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.MessageId
import com.spruhs.kick_app.common.MessageNotFoundException
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.domain.Message
import com.spruhs.kick_app.user.core.domain.MessagePersistencePort
import com.spruhs.kick_app.user.core.domain.UserInvitedToGroupMessage
import org.springframework.stereotype.Service

@Service
class MessageUseCases(val messagePersistencePort: MessagePersistencePort) {

    fun send(messageType: MessageType, params: MessageParams): Message {
        return createMessage(messageType, params).apply {
            messagePersistencePort.save(this)
        }
    }

    fun getByUser(userId: UserId): List<Message> {
        return messagePersistencePort.findByUser(userId)
    }

    fun markAsRead(command: MarkAsReadCommand) {
        messagePersistencePort.findById(command.messageId)?.let {
            it.messageReadBy(command.userId)
            messagePersistencePort.save(it)
        } ?: throw MessageNotFoundException(command.messageId)
    }
}

data class MarkAsReadCommand(
    val messageId: MessageId,
    val userId: UserId
)

data class MessageParams(
    val userId: String? = null,
    val groupId: String? = null,
    val groupName: String? = null
)

enum class MessageType {
    USER_INVITED_TO_GROUP
}

fun createMessage(type: MessageType, params: MessageParams): Message {
    return when (type) {
        MessageType.USER_INVITED_TO_GROUP -> UserInvitedToGroupMessage(
            userId = params.userId ?: throw IllegalArgumentException("userId is required"),
            groupId = params.groupId ?: throw IllegalArgumentException("groupId is required"),
            groupName = params.groupName ?: throw IllegalArgumentException("groupName is required")
        )
    }
}