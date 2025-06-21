package com.spruhs.kick_app.user

import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserImageId
import com.spruhs.kick_app.view.api.UserData
import com.spruhs.kick_app.user.core.adapter.primary.RegisterUserRequest
import com.spruhs.kick_app.user.core.domain.Email
import com.spruhs.kick_app.user.core.domain.NickName
import com.spruhs.kick_app.user.core.domain.UserAggregate
import com.spruhs.kick_app.view.core.controller.rest.UserMessage
import com.spruhs.kick_app.view.core.service.UserProjection

class TestUserBuilder {
    var id =  "testUserId"
    var nickName = "testNickName"
    var email = "test@testen.com"
    var imageId = "testImageId"

    fun buildAggregate(): UserAggregate {
        val user =  UserAggregate(id)
        user.email = Email(email)
        user.nickName = NickName(nickName)
        user.userImageId = UserImageId(imageId)
        return user
    }

    fun buildData(): UserData {
        return UserData(
            id = UserId(id),
            nickName = nickName,
        )
    }

    fun buildMessage(): UserMessage {
        return UserMessage(
            id = id,
            nickName = nickName,
            email = email,
            imageId = imageId
        )
    }

    fun buildProjection(): UserProjection {
        return UserProjection(
            id = UserId(id),
            nickName = nickName,
            email = email,
            userImageId = UserImageId(imageId)
        )
    }

    fun buildRegisterUserRequest(): RegisterUserRequest {
        return RegisterUserRequest(
            nickName = nickName,
            email = email,
            password = null
        )
    }

    fun withId(id: String) = this.apply { this.id = id }
    fun withNickName(nickName: String) = this.apply { this.nickName = nickName }
    fun withEmail(email: String) = this.apply { this.email = email }
    fun withImageId(imageId: String) = this.apply { this.imageId = imageId }
}