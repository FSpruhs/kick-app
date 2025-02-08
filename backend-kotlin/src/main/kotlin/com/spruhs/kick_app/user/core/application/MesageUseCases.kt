package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.group.api.GroupApi
import com.spruhs.kick_app.user.core.domain.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MessageUseCases(
    private val messagePersistencePort: MessagePersistencePort,
    private val groupApi: GroupApi,
) {

    fun send(messageType: MessageType, params: MessageParams) {
        createMessage(messageType, params).apply {
            messagePersistencePort.save(this)
        }
    }

    fun getByUser(userId: UserId): List<Message> {
        return messagePersistencePort.findByUser(userId)
    }

    fun markAsRead(command: MarkAsReadCommand) {
        fetchMessage(command.messageId).let {
            it.messageReadBy(command.userId)
            messagePersistencePort.save(it)
        }
    }

    fun sendAllActiveUsersInGroupMessage(
        messageType: MessageType,
        params: MessageParams,
        groupId: GroupId
    ) {
        groupApi.getActivePlayers(groupId)
            .map { createMessage(messageType, params.copy(userId = it.value)) }
            .toList()
            .let { messagePersistencePort.saveAll(it) }
    }

    private fun fetchMessage(messageId: MessageId): Message =
        messagePersistencePort.findById(messageId) ?: throw MessageNotFoundException(messageId)

}

data class MarkAsReadCommand(
    val messageId: MessageId,
    val userId: UserId
)

data class MessageParams(
    val userId: String? = null,
    val groupId: String? = null,
    val groupName: String? = null,
    val matchId: String? = null,
    val start: LocalDateTime? = null
)

private fun createMessage(type: MessageType, params: MessageParams): Message {
    return when (type) {
        MessageType.USER_INVITED_TO_GROUP -> MessageFactory().createUserInvitedToGroupMessage(params)
        MessageType.USER_LEAVED_GROUP -> MessageFactory().createUserLeavedGroupMessage(params)
        MessageType.USER_REMOVED_FROM_GROUP -> MessageFactory().createUserRemovedFromGroupMessage(params)
        MessageType.MATCH_CREATED -> MessageFactory().createMatchCreatedMessage(params)
    }
}

private const val GROUP_ID = "groupId"
private const val MATCH_ID = "matchId"

class MessageFactory {
    fun createUserInvitedToGroupMessage(params: MessageParams): Message {
        require(!params.userId.isNullOrBlank())
        require(!params.groupId.isNullOrBlank())
        require(!params.groupName.isNullOrBlank())
        return Message(
            id = MessageId(generateId()),
            text = "You have been invited to group ${params.groupName}",
            type = MessageType.USER_INVITED_TO_GROUP,
            user = UserId(params.userId),
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId)
        )
    }

    fun createUserLeavedGroupMessage(params: MessageParams): Message {
        require(!params.userId.isNullOrBlank())
        require(!params.groupId.isNullOrBlank())
        require(!params.groupName.isNullOrBlank())
        return Message(
            id = MessageId(generateId()),
            text = "You have leaved group ${params.groupName}",
            type = MessageType.USER_LEAVED_GROUP,
            user = UserId(params.userId),
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId)
        )
    }

    fun createUserRemovedFromGroupMessage(params: MessageParams): Message {
        require(!params.userId.isNullOrBlank())
        require(!params.groupId.isNullOrBlank())
        require(!params.groupName.isNullOrBlank())
        return Message(
            id = MessageId(generateId()),
            text = "You have been removed from group ${params.groupName}",
            type = MessageType.USER_REMOVED_FROM_GROUP,
            user = UserId(params.userId),
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId)
        )
    }

    fun createMatchCreatedMessage(params: MessageParams): Message {
        require(!params.userId.isNullOrBlank())
        require(!params.groupId.isNullOrBlank())
        require(!params.matchId.isNullOrBlank())
        return Message(
            id = MessageId(generateId()),
            text = "Invented for match on ${params.start}",
            type = MessageType.MATCH_CREATED,
            user = UserId(params.userId),
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId, MATCH_ID to params.matchId)
        )
    }
}
