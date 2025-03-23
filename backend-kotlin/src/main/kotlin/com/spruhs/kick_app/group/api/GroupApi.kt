package com.spruhs.kick_app.group.api

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId

interface GroupApi {
    suspend fun isActiveMember(groupId: GroupId, userId: UserId): Boolean
    suspend fun areActiveMembers(groupId: GroupId, userIds: Set<UserId>): Boolean
    suspend fun isActiveAdmin(groupId: GroupId, userId: UserId): Boolean
    suspend fun getActivePlayers(groupId: GroupId): List<UserId>
}