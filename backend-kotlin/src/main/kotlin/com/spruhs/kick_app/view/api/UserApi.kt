package com.spruhs.kick_app.view.api

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.UserImageId

interface UserApi {
    suspend fun findUserById(userId: UserId): UserData
    suspend fun getGroups(userId: UserId): List<GroupId>
    suspend fun existsByEmail(email: String): Boolean
}

data class UserData(
    val id: UserId,
    val email: String,
    val nickName: String,
    val imageId: UserImageId?,
)