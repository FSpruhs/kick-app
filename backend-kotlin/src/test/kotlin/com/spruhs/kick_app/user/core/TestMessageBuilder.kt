package com.spruhs.kick_app.user.core

import com.spruhs.kick_app.common.MessageId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.domain.Message
import com.spruhs.kick_app.user.core.domain.MessageType
import java.time.LocalDateTime

class TestMessageBuilder {

    private var id: String = "Test id"
    private var user: String = "Test user"
    private val text: String = "Test text"
    private val timeStamp: LocalDateTime = LocalDateTime.now()
    private var isRead: Boolean = true
    private var type: MessageType = MessageType.USER_INVITED_TO_GROUP
    private val variables: Map<String, String> = mapOf("Test key" to "Test value")

    fun build(): Message {
        return Message(
            id = MessageId(id),
            user = UserId(user),
            text = text,
            timeStamp = timeStamp,
            isRead = isRead,
            type = type,
            variables = variables
        )
    }

    fun withId(id: String) = apply { this.id = id }
    fun withUserId(user: String) = apply { this.user = user }
    fun withType(type: MessageType) = apply { this.type = type }
    fun withIsRead(isRead: Boolean) = apply { this.isRead = isRead }
}