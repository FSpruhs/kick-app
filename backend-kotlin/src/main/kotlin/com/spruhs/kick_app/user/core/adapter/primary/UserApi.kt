package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.api.UserApi
import com.spruhs.kick_app.user.api.UserData
import com.spruhs.kick_app.user.core.application.UserQueryPort
import com.spruhs.kick_app.user.core.domain.UserProjection
import org.springframework.stereotype.Service

@Service
class UserApiAdapter(private val userQueryPort: UserQueryPort) : UserApi {
    override suspend fun findUsersByIds(userIds: List<UserId>): List<UserData> =
        userQueryPort.getUsersByIds(userIds).map { it.toData() }

    override suspend fun findUserById(userId: UserId): UserData = userQueryPort.getUser(userId).toData()

}

private fun UserProjection.toData() = UserData(this.id, this.nickName.value)