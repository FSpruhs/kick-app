package com.spruhs.kick_app.user

import com.spruhs.kick_app.common.types.MessageId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.user.core.domain.Message
import com.spruhs.kick_app.user.core.domain.MessageType
import java.time.LocalDateTime

class TestMessageBuilder {

    var id = MessageId("1")
    var text = "Hello World"
    var user = UserId("user1")
    var timeStamp = LocalDateTime.now()
    var type = MessageType.USER_INVITED_TO_GROUP
    var isRead = false
    var variables = mapOf("key1" to "value1", "key2" to "value2")

    fun build(): Message {
        return Message(
            id = id,
            text = text,
            user = user,
            timeStamp = timeStamp,
            type = type,
            isRead = isRead,
            variables = variables
        )
    }

    fun withId(id: MessageId) = this.apply { this.id = id }
    fun withUserId(userId: UserId) = this.apply { this.user = userId }
}