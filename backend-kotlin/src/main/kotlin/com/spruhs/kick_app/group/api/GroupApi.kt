package com.spruhs.kick_app.group.api

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId

interface GroupApi {
    fun isActiveMember(groupId: GroupId, userId: UserId): Boolean
    fun areActiveMembers(groupId: GroupId, userIds: Set<UserId>): Boolean
    fun isActiveAdmin(groupId: GroupId, userId: UserId): Boolean
    fun getActivePlayers(groupId: GroupId): List<UserId>
}