package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.MessageId
import com.spruhs.kick_app.common.MessageNotFoundException
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.domain.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

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

fun createMessage(type: MessageType, params: MessageParams): Message {
    return when (type) {
        MessageType.USER_INVITED_TO_GROUP -> MessageFactory().createUserInvitedToGroupMessage(params)
        MessageType.USER_LEAVED_GROUP -> MessageFactory().createUserLeavedGroupMessage(params)
        MessageType.USER_REMOVED_FROM_GROUP -> MessageFactory().createUserRemovedFromGroupMessage(params)
    }
}

private const val GROUP_ID = "groupId"

class MessageFactory {
    fun createUserInvitedToGroupMessage(params: MessageParams): Message {
        require(!params.userId.isNullOrBlank())
        require(!params.groupId.isNullOrBlank())
        require(!params.groupName.isNullOrBlank())
        return Message(
            id = MessageId(UUID.randomUUID().toString()),
            text = "You have been invited to group ${params.groupName}",
            type = MessageType.USER_INVITED_TO_GROUP,
            user = UserId(params.userId),
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId)
        )
    }

    fun createUserLeavedGroupMessage(params: MessageParams): Message {
        require(!params.userId.isNullOrBlank())
        require(!params.groupId.isNullOrBlank())
        require(!params.groupName.isNullOrBlank())
        return Message(
            id = MessageId(UUID.randomUUID().toString()),
            text = "You have leaved group ${params.groupName}",
            type = MessageType.USER_LEAVED_GROUP,
            user = UserId(params.userId),
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId)
        )
    }

    fun createUserRemovedFromGroupMessage(params: MessageParams): Message {
        require(!params.userId.isNullOrBlank())
        require(!params.groupId.isNullOrBlank())
        require(!params.groupName.isNullOrBlank())
        return Message(
            id = MessageId(UUID.randomUUID().toString()),
            text = "You have been removed from group ${params.groupName}",
            type = MessageType.USER_REMOVED_FROM_GROUP,
            user = UserId(params.userId),
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId)
        )
    }
}
