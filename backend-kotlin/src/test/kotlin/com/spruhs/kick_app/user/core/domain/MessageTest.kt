package com.spruhs.kick_app.user.core.domain

import com.spruhs.kick_app.common.MessageId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class MessageTest {

    @Test
    fun `messageReadBy should set isRead to true`() {
        // Given
        val userId = UserId("test user")
        val message = Message(
            id = MessageId("test id"),
            text = "test text",
            user = userId,
            type = MessageType.USER_INVITED_TO_GROUP,
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = emptyMap()
        )

        // When
        val updatedMessage = message.messageReadBy(userId)

        // Then
        assertThat(updatedMessage.isRead).isTrue()
    }

    @Test
    fun `messageReadBy should throw UserNotAuthorizedException if userId does not match`() {
        // Given
        val userId = UserId("test user")
        val message = Message(
            id = MessageId("test id"),
            text = "test text",
            user = userId,
            type = MessageType.USER_INVITED_TO_GROUP,
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = emptyMap()
        )

        // When
        assertThatThrownBy {
            message.messageReadBy(UserId("different user"))
        }.isInstanceOf(UserNotAuthorizedException::class.java)
    }

}