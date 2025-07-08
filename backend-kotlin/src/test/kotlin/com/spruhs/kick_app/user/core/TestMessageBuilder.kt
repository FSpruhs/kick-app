package com.spruhs.kick_app.user.core

import com.spruhs.kick_app.common.types.MessageId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.message.core.domain.Message
import com.spruhs.kick_app.message.core.domain.MessageType
import java.time.LocalDateTime

class TestMessageBuilder {

    var id: String = "messageId"
    var text: String = "messageText"
    var user: String = "userId"
    var type: MessageType = MessageType.USER_INVITED_TO_GROUP
    var timeStamp: LocalDateTime = LocalDateTime.now()
    var isRead: Boolean = false
    var variables: Map<String, String> = emptyMap()

    fun buildMessage(): Message {
        return Message(
            id = MessageId(this.id),
            text = this.text,
            user = UserId(this.user),
            type = this.type,
            timeStamp = this.timeStamp,
            isRead = this.isRead,
            variables = this.variables
        )
    }

    fun withId(id: String) = apply { this.id = id }
    fun withText(text: String) = apply { this.text = text }
    fun withUser(user: String) = apply { this.user = user }
    fun withType(type: MessageType) = apply { this.type = type }
    fun withTimeStamp(timeStamp: LocalDateTime) = apply { this.timeStamp = timeStamp }
    fun withIsRead(isRead: Boolean) = apply { this.isRead = isRead }
    fun withVariables(variables: Map<String, String>) = apply { this.variables = variables }
}