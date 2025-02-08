package com.spruhs.kick_app.user.core.domain

import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.user.core.TestMessageBuilder
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MessageTest {
    @Test
    fun `messageReadBy should mark message as read`() {
        // Given
        val message = TestMessageBuilder()
            .withIsRead(false)
            .build()

        // When
        message.messageReadBy(message.user).let { result ->
            // Then
            assertTrue(result.isRead)
        }
    }

    @Test
    fun `messageReadBy should throw UserNotAuthorizedException when user is not authorized`() {
        // Given
        val message = TestMessageBuilder().build()
        val userId = UserId("Another user")

        // When
        assertThatThrownBy { message.messageReadBy(userId) }
            // Then
            .isInstanceOf(UserNotAuthorizedException::class.java)
    }
}