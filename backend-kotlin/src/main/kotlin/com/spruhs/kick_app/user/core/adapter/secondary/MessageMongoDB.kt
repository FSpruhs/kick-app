package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.domain.Message
import com.spruhs.kick_app.user.core.domain.MessagePersistencePort
import org.springframework.stereotype.Service

@Service
class MessagePersistenceAdapter : MessagePersistencePort {
    override fun save(message: Message) {
        TODO("Not yet implemented")
    }

    override fun findByUser(userId: UserId): List<Message> {
        TODO("Not yet implemented")
    }
}