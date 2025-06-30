package com.spruhs.kick_app.view.api

import com.spruhs.kick_app.common.UserId

interface UserApi {
    suspend fun findUserById(userId: UserId): UserData
    suspend fun existsByEmail(email: String): Boolean
}

data class UserData(
    val id: UserId,
    val email: String,
    val nickName: String,
)