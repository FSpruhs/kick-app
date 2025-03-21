package com.spruhs.kick_app.user.core.domain

import com.spruhs.kick_app.common.MessageId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import java.time.LocalDateTime

data class Message(
    val id: MessageId,
    val text: String,
    val user: UserId,
    val type: MessageType,
    val timeStamp: LocalDateTime,
    val isRead: Boolean,
    val variables: Map<String, String>,
)

fun Message.messageReadBy(userId: UserId): Message {
    require(userId == this.user) { throw UserNotAuthorizedException(userId) }

    return this.copy(isRead = true)
}

enum class MessageType {
    USER_INVITED_TO_GROUP,
    USER_LEAVED_GROUP,
    USER_REMOVED_FROM_GROUP,
    MATCH_CREATED,
}

interface MessagePersistencePort {
    suspend fun save(message: Message)
    suspend fun saveAll(messages: List<Message>)
    suspend fun findById(messageId: MessageId): Message?
    suspend fun findByUser(userId: UserId): List<Message>
}
