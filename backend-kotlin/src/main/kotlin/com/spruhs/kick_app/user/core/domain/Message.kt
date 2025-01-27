package com.spruhs.kick_app.user.core.domain

import com.spruhs.kick_app.common.MessageId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import java.time.LocalDateTime
import java.util.UUID

object MessageVariables {
    const val GROUP_NAME = "groupName"
    const val GROUP_ID = "groupId"
    const val USER_ID = "userId"
}

abstract class Message(
    open val id: MessageId,
    open val text: String,
    open val user: UserId,
    open val timeStamp: LocalDateTime,
    open var isRead: Boolean,
    open val variables: Map<String, String>,
    ) {
    fun messageReadBy(userId: UserId) {
        require(userId == this.user) { UserNotAuthorizedException(userId) }

        this.isRead = true
    }
}

class UserInvitedToGroupMessage(
    override val id: MessageId,
    override val text: String,
    override val user: UserId,
    override val timeStamp: LocalDateTime,
    override var isRead: Boolean,
    override val variables: Map<String, String>
) : Message(id, text, user, timeStamp, isRead, variables) {

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
    fun findById(messageId: MessageId): Message?
    fun findByUser(userId: UserId): List<Message>
}
