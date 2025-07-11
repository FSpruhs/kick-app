package com.spruhs.kick_app.user

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.UserImageId
import com.spruhs.kick_app.view.api.UserData
import com.spruhs.kick_app.user.core.adapter.primary.RegisterUserRequest
import com.spruhs.kick_app.user.core.domain.Email
import com.spruhs.kick_app.user.core.domain.NickName
import com.spruhs.kick_app.user.core.domain.UserAggregate
import com.spruhs.kick_app.view.core.controller.rest.UserMessage
import com.spruhs.kick_app.view.core.service.UserGroupProjection
import com.spruhs.kick_app.view.core.service.UserProjection
import java.time.LocalDateTime

class TestUserBuilder {
    var id = "testUserId"
    var nickName = "testNickName"
    var email = "test@testen.com"
    var imageId: String? = "testImageId"
    var groups = listOf(UserGroupProjection(
        id = GroupId("testGroupId"),
        name = "testGroupName",
        userStatus = PlayerStatusType.ACTIVE,
        userRole = PlayerRole.PLAYER,
        lastMatch = LocalDateTime.now(),
    ))

    fun buildAggregate(): UserAggregate {
        val user =  UserAggregate(id)
        user.email = Email(email)
        user.nickName = NickName(nickName)
        user.userImageId = imageId?.let { UserImageId(it) }
        return user
    }

    fun buildData(): UserData {
        return UserData(
            id = UserId(id),
            nickName = nickName,
            email = email,
            imageId = imageId?.let { UserImageId(it) }
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
            userImageId = imageId?.let { UserImageId(it) },
            groups = groups
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
    fun withGroups(groups: List<UserGroupProjection>) = this.apply { this.groups = groups }
    fun withNickName(nickName: String) = this.apply { this.nickName = nickName }
    fun withEmail(email: String) = this.apply { this.email = email }
    fun withImageId(imageId: String?) = this.apply { this.imageId = imageId }
}