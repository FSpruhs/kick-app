package com.spruhs.kick_app.view.api

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId

interface GroupApi {
    suspend fun isActiveMember(groupId: GroupId, userId: UserId): Boolean
    suspend fun isActiveCoach(groupId: GroupId, userId: UserId): Boolean
    suspend fun getActivePlayers(groupId: GroupId): List<UserId>
    suspend fun getGroupNameList(groupId: GroupId): Map<UserId, String>
    suspend fun getUserGroups(userId: UserId): List<GroupId>
}