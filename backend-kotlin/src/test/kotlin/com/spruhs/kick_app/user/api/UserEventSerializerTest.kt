package com.spruhs.kick_app.user.api

import com.spruhs.kick_app.user.core.domain.UserAggregate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class UserEventSerializerTest {

    @ParameterizedTest
    @MethodSource("data")
    fun `serialize and deserialize user events`(event: Any) {
        // Given
        val userId = "userId"
        val aggregate = UserAggregate(userId)
        val event = event

        // When
        val serialized = UserEventSerializer().serialize(event, aggregate)
        val deserialized = UserEventSerializer().deserialize(serialized)

        // Then
        assertThat(deserialized).isEqualTo(event)
    }

    companion object {
        @JvmStatic
        fun data() = listOf(
            UserCreatedEvent("userId", "email", "nickName"),
            UserNickNameChangedEvent("userId", "newNickName"),
        )
    }
}