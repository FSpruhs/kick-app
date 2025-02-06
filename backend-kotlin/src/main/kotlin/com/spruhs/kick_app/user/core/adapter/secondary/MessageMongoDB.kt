package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.MessageId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.domain.Message
import com.spruhs.kick_app.user.core.domain.MessagePersistencePort
import com.spruhs.kick_app.user.core.domain.MessageType
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MessagePersistenceAdapter(val messageRepository: MessageRepository) : MessagePersistencePort {
    override fun save(message: Message) {
        messageRepository.save(message.toDocument())
    }

    override fun findById(messageId: MessageId): Message? {
        return messageRepository.findById(messageId.value).orElse(null)?.toDomain()
    }

    override fun findByUser(userId: UserId): List<Message> {
        return messageRepository.findByUserId(userId.value).map { it.toDomain() }
    }
}

@Repository
interface MessageRepository : MongoRepository<MessageDocument, String> {
    fun findByUserId(userId: String): List<MessageDocument>
}

data class MessageDocument(
    val id: String,
    val userId: String,
    val content: String,
    val type: String,
    val timeStamp: String,
    val isRead: Boolean,
    val variables: Map<String, String>
)

private fun Message.toDocument() = MessageDocument(
    id = id.value,
    userId = user.value,
    content = text,
    timeStamp = timeStamp.toString(),
    isRead = isRead,
    type = type.toString(),
    variables = variables
)

private fun MessageDocument.toDomain() = Message(
    id = MessageId(id),
    user = UserId(userId),
    text = content,
    timeStamp = LocalDateTime.parse(timeStamp),
    isRead = isRead,
    type = MessageType.valueOf(this.type),
    variables = variables
)