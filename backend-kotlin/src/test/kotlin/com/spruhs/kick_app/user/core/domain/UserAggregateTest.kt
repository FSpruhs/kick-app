package com.spruhs.kick_app.user.core.domain

import com.spruhs.kick_app.common.types.Email
import com.spruhs.kick_app.common.types.UserImageId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserAggregateTest {
    @Test
    fun `createUser should create user`() {
        // Given
        val userId = "user-123"
        val userAggregate = UserAggregate(userId)

        val nickName = NickName("testNickName")
        val email = Email("test@testen.com")

        // When
        userAggregate.createUser(email, nickName)

        // Then
        assertThat(userAggregate.aggregateId).isEqualTo(userId)
        assertThat(userAggregate.nickName).isEqualTo(nickName)
        assertThat(userAggregate.email).isEqualTo(email)
        assertThat(userAggregate.userImageId).isNull()
    }

    @Test
    fun `changeNickName should change user nickname`() {
        // Given
        val userId = "user-123"
        val userAggregate = UserAggregate(userId)

        val nickName = NickName("newNickName")

        // When
        userAggregate.changeNickName(nickName)

        // Then
        assertThat(userAggregate.nickName).isEqualTo(nickName)
    }

    @Test
    fun `updateUserImage should update user image`() {
        // Given
        val userId = "user-123"
        val userAggregate = UserAggregate(userId)
        val imageId = UserImageId("image-123.svg")

        // When
        userAggregate.updateUserImage(imageId)

        // Then
        assertThat(userAggregate.userImageId).isEqualTo(imageId)
    }
}
