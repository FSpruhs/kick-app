package com.spruhs.kick_app.message.core.domain

import com.spruhs.kick_app.common.types.MessageId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.exceptions.UserNotAuthorizedException
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
    USER_DOWNGRADED,
    USER_PROMOTED,
    MATCH_CANCELED,
    PLAYGROUND_CHANGED,
    PLAYER_ADDED_TO_CADRE,
    PLAYER_PLACED_ON_WAITING_BENCH,
}

interface MessagePersistencePort {
    suspend fun save(message: Message)
    suspend fun saveAll(messages: List<Message>)
    suspend fun findById(messageId: MessageId): Message?
    suspend fun findByUser(userId: UserId): List<Message>
}
