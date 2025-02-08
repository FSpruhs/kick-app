package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.AbstractMongoTest
import com.spruhs.kick_app.user.core.TestMessageBuilder
import com.spruhs.kick_app.user.core.domain.MessagePersistencePort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MessagePersistenceAdapterTest : AbstractMongoTest() {

    @Autowired
    private lateinit var messagePersistencePort: MessagePersistencePort

    @Test
    fun `save should save message`() {
        // Given
        val message = TestMessageBuilder().build()

        // When
        messagePersistencePort.save(message)

        // Then
        messagePersistencePort.findById(message.id).let { result ->
            assertThat(result).isEqualTo(message)
        }
    }

    @Test
    fun `save all should save all messages`() {
        // Given
        val messages = listOf(
            TestMessageBuilder().withId("1").build(),
            TestMessageBuilder().withId("2").build()
        )

        // When
        messagePersistencePort.saveAll(messages)

        // Then
        messages.forEach { message ->
            messagePersistencePort.findById(message.id).let { result ->
                assertThat(result).isEqualTo(message)
            }
        }
    }

    @Test
    fun `find by user should return messages for user`() {
        // Given
        val message1 = TestMessageBuilder()
            .withId("1")
            .withUserId("user id 1")
            .build()
        val message2 = TestMessageBuilder()
            .withId("2")
            .withUserId("user id 2")
            .build()

        // When
        messagePersistencePort.save(message1)
        messagePersistencePort.save(message2)

        // Then
        messagePersistencePort.findByUser(message1.user).let { result ->
            assertThat(result).containsExactly(message1)
        }
    }
}