package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.api.GroupApi
import com.spruhs.kick_app.group.core.application.GroupUseCases
import org.springframework.stereotype.Service

@Service
class GroupApiAdapter(private val groupUseCases: GroupUseCases) : GroupApi {
    override fun isActiveMember(groupId: GroupId, userId: UserId): Boolean {
        return groupUseCases.isActiveMember(groupId, userId)
    }
}