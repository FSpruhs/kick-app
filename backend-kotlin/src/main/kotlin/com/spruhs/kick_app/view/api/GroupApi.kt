package com.spruhs.kick_app.view.api

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId

interface GroupApi {
    suspend fun isActiveMember(groupId: GroupId, userId: UserId): Boolean
    suspend fun isActiveCoach(groupId: GroupId, userId: UserId): Boolean
    suspend fun getActivePlayers(groupId: GroupId): List<UserId>
}