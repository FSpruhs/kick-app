package com.spruhs.kick_app.user.core.domain

import com.spruhs.kick_app.common.MessageId
import com.spruhs.kick_app.common.UserId
import java.time.LocalDateTime
import java.util.UUID

object MessageVariables {
    const val GROUP_NAME = "groupName"
    const val GROUP_ID = "groupId"
    const val USER_ID = "userId"
}

interface Message {
    val id: MessageId
    val text: String
    val user: UserId
    val timeStamp: LocalDateTime
    val isRead: Boolean
    val variables: Map<String, String>
}

class UserInvitedToGroupMessage(
    override val id: MessageId,
    override val text: String,
    override val user: UserId,
    override val timeStamp: LocalDateTime,
    override val isRead: Boolean,
    override val variables: Map<String, String>
) : Message {
    constructor(userId: String, groupId: String, groupName: String) : this(
        id = MessageId(UUID.randomUUID().toString()),
        text = "You have been invited to group $groupName",
        user = UserId(userId),
        timeStamp = LocalDateTime.now(),
        isRead = false,
        variables = mapOf(MessageVariables.GROUP_ID to groupId)
    )
}

interface MessagePersistencePort {
    fun save(message: Message)
    fun findByUser(userId: UserId): List<Message>
}
