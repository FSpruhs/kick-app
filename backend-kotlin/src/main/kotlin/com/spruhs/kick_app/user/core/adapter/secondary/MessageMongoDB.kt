package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.MessageId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.domain.Message
import com.spruhs.kick_app.user.core.domain.MessagePersistencePort
import com.spruhs.kick_app.user.core.domain.MessageType
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MessagePersistenceAdapter(private val messageRepository: MessageRepository) : MessagePersistencePort {
    override fun save(message: Message) {
        messageRepository.save(message.toDocument())
    }

    override fun saveAll(messages: List<Message>) {
        messageRepository.saveAll(messages.map { it.toDocument() })
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

@Document(collation = "messages")
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
    id = this.id.value,
    userId = this.user.value,
    content = this.text,
    timeStamp = this.timeStamp.toString(),
    isRead = this.isRead,
    type = this.type.toString(),
    variables = this.variables
)

private fun MessageDocument.toDomain() = Message(
    id = MessageId(this.id),
    user = UserId(this.userId),
    text = this.content,
    timeStamp = LocalDateTime.parse(this.timeStamp),
    isRead = this.isRead,
    type = MessageType.valueOf(this.type),
    variables = this.variables
)