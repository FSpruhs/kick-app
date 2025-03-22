package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.api.UserApi
import com.spruhs.kick_app.user.api.UserData
import com.spruhs.kick_app.user.core.application.UserUseCases
import com.spruhs.kick_app.user.core.domain.User
import org.springframework.stereotype.Service

@Service
class UserApiAdapter(private val userUseCases: UserUseCases) : UserApi {
    override suspend fun findUsersByIds(userIds: List<UserId>): List<UserData> =
        userUseCases.getUsersByIds(userIds).map { it.toData() }

    override suspend fun findUserById(userId: UserId): UserData = userUseCases.getUser(userId).toData()

}

private fun User.toData() = UserData(this.id, this.nickName.value)