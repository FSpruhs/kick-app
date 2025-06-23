package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.AbstractMongoTest
import com.spruhs.kick_app.common.MessageId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.TestMessageBuilder
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MessagePersistenceAdapterTest : AbstractMongoTest() {

    @Autowired
    private lateinit var adapter: MessagePersistenceAdapter

    @Autowired
    private lateinit var messageRepository: MessageRepository

    @Test
    fun `save should save message`(): Unit = runBlocking {
        // Given
        val message = TestMessageBuilder().build()

        // When
        adapter.save(message)

        // Then
        adapter.findById(message.id).let { result ->
            assertNotNull(result)
            assertThat(result).isNotNull
            assertThat(result?.id).isEqualTo(message.id)
            assertThat(result?.text).isEqualTo(message.text)
            assertThat(result?.user).isEqualTo(message.user)
            assertThat(result?.timeStamp).isEqualTo(message.timeStamp)
            assertThat(result?.type).isEqualTo(message.type)
            assertThat(result?.isRead).isEqualTo(message.isRead)
            assertThat(result?.variables).isEqualTo(message.variables)
        }
    }

    @Test
    fun `findByUser should find by user`(): Unit = runBlocking {
        // Given
        val userId = UserId("testUser")
        val message1 = TestMessageBuilder()
            .withId(MessageId("m1"))
            .withUserId(userId)
            .build()
        val message2 = TestMessageBuilder()
            .withId(MessageId("m2"))
            .withUserId(UserId("user2"))
            .build()

        // When
        adapter.saveAll(listOf(message1, message2))

        // Then
        adapter.findByUser(userId).let { result ->
            assertNotNull(result)
            assertThat(result).hasSize(1)
            assertThat(result.first().user).isEqualTo(userId)

        }
    }

}