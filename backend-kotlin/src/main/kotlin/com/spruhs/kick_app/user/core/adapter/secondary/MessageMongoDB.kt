package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.MessageId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.domain.Message
import com.spruhs.kick_app.user.core.domain.MessagePersistencePort
import com.spruhs.kick_app.user.core.domain.MessageType
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitLast
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Service
class MessagePersistenceAdapter(private val messageRepository: MessageRepository) : MessagePersistencePort {
    override suspend fun save(message: Message) {
        messageRepository.save(message.toDocument()).awaitFirstOrNull()
    }

    override suspend fun saveAll(messages: List<Message>) {
        messageRepository.saveAll(messages.map { it.toDocument() }).collectList().awaitSingle()
    }

    override suspend fun findById(messageId: MessageId): Message? {
        return messageRepository.findById(messageId.value).awaitFirstOrNull()?.toDomain()
    }

    override suspend fun findByUser(userId: UserId): List<Message> {
        return messageRepository.findByUserId(userId.value).collectList().awaitSingle().map { it.toDomain() }
    }
}

@Repository
interface MessageRepository : ReactiveMongoRepository<MessageDocument, String> {
    fun findByUserId(userId: String): Flux<MessageDocument>
}

@Document(collection = "messages")
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