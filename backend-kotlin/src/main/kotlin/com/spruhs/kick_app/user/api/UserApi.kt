package com.spruhs.kick_app.user.api

import com.spruhs.kick_app.common.UserId

interface UserApi {
    suspend fun findUsersByIds(userIds: List<UserId>): List<UserData>
    suspend fun findUserById(userId: UserId): UserData
}

data class UserData(
    val id: UserId,
    val nickName: String,
)