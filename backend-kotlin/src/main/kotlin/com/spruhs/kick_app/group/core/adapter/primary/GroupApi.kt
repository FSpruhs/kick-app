package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.api.GroupApi
import com.spruhs.kick_app.group.core.application.GroupQueryPort
import org.springframework.stereotype.Service

@Service
class GroupApiAdapter(private val groupQueryPort: GroupQueryPort) : GroupApi {
    override suspend fun isActiveMember(groupId: GroupId, userId: UserId): Boolean =
        groupQueryPort.isActiveMember(groupId, userId)

    override suspend fun isActiveAdmin(groupId: GroupId, userId: UserId): Boolean =
        groupQueryPort.isActiveAdmin(groupId, userId)


    override suspend fun getActivePlayers(groupId: GroupId): List<UserId> =
        groupQueryPort.getActivePlayers(groupId)
}