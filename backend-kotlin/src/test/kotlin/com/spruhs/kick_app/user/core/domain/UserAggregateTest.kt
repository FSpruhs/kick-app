package com.spruhs.kick_app.user.core.domain

import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.UserImageId
import com.spruhs.kick_app.user.core.application.ChangeUserNickNameCommand
import com.spruhs.kick_app.user.core.application.RegisterUserCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserAggregateTest {

    @Test
    fun `createUser should create user`() {
        // Given
        val userId = "user-123"
        val userAggregate = UserAggregate(userId)
        val command = RegisterUserCommand(
            nickName = NickName("testNickName"),
            email = Email("test@testen.com"),
        )

        // When
        userAggregate.createUser(command)

        // Then
        assertThat(userAggregate.aggregateId).isEqualTo(userId)
        assertThat(userAggregate.nickName).isEqualTo(command.nickName)
        assertThat(userAggregate.email).isEqualTo(command.email)
        assertThat(userAggregate.userImageId).isNull()
    }

    @Test
    fun `changeNickName should change user nickname`() {
        // Given
        val userId = "user-123"
        val userAggregate = UserAggregate(userId)
        val command = ChangeUserNickNameCommand(
            userId = UserId(userId),
            nickName = NickName("newNickName")
        )

        // When
        userAggregate.changeNickName(command)

        // Then
        assertThat(userAggregate.nickName).isEqualTo(command.nickName)
    }

    @Test
    fun `updateUserImage should update user image`() {
        // Given
        val userId = "user-123"
        val userAggregate = UserAggregate(userId)
        val imageId = UserImageId("image-123")

        // When
        userAggregate.updateUserImage(imageId)

        // Then
        assertThat(userAggregate.userImageId).isEqualTo(imageId)
    }

}