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

    suspend fun send(messageType: MessageType, params: MessageParams) {
        createMessage(messageType, params).apply {
            messagePersistencePort.save(this)
        }
    }

    suspend fun getByUser(userId: UserId): List<Message> = messagePersistencePort.findByUser(userId)

    suspend fun markAsRead(command: MarkAsReadCommand) {
        fetchMessage(command.messageId).let { message ->
            message.messageReadBy(command.userId).also {
                messagePersistencePort.save(it)
            }
        }
    }

    suspend fun sendAllActiveUsersInGroupMessage(
        messageType: MessageType,
        params: MessageParams,
        groupId: GroupId
    ) {
        groupApi.getActivePlayers(groupId)
            .map { createMessage(messageType, params.copy(userId = it)) }
            .toList()
            .let { messagePersistencePort.saveAll(it) }
    }

    private suspend fun fetchMessage(messageId: MessageId): Message =
        messagePersistencePort.findById(messageId) ?: throw MessageNotFoundException(messageId)
}

data class MarkAsReadCommand(
    val messageId: MessageId,
    val userId: UserId
)

data class MessageParams(
    val userId: UserId? = null,
    val groupId: GroupId? = null,
    val groupName: String? = null,
    val matchId: MatchId? = null,
    val start: LocalDateTime? = null,
    val playground: String? = null,
)

private fun createMessage(type: MessageType, params: MessageParams): Message {
    return when (type) {
        MessageType.USER_INVITED_TO_GROUP -> MessageFactory().createUserInvitedToGroupMessage(params)
        MessageType.USER_LEAVED_GROUP -> MessageFactory().createUserLeavedGroupMessage(params)
        MessageType.USER_REMOVED_FROM_GROUP -> MessageFactory().createUserRemovedFromGroupMessage(params)
        MessageType.MATCH_CREATED -> MessageFactory().createMatchCreatedMessage(params)
        MessageType.USER_DOWNGRADED -> MessageFactory().createUserDowngradedMessage(params)
        MessageType.USER_PROMOTED -> MessageFactory().createUserPromotedMessage(params)
        MessageType.MATCH_CANCELED -> MessageFactory().createMatchCanceledMessage(params)
        MessageType.PLAYGROUND_CHANGED -> MessageFactory().createPlaygroundChangedMessage(params)
        MessageType.PLAYER_ADDED_TO_CADRE -> MessageFactory().createPlayerAddedToCadreMessage(params)
        MessageType.PLAYER_PLACED_ON_WAITING_BENCH -> MessageFactory().createPlayerPlacedOnWaitingBenchMessage(params)
    }
}

private const val GROUP_ID = "groupId"
private const val MATCH_ID = "matchId"

class MessageFactory {
    fun createUserInvitedToGroupMessage(params: MessageParams): Message {
        require(params.userId != null)
        require(params.groupId != null)
        require(!params.groupName.isNullOrBlank())
        return Message(
            id = MessageId(generateId()),
            text = "You have been invited to group ${params.groupName}",
            type = MessageType.USER_INVITED_TO_GROUP,
            user = params.userId,
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value)
        )
    }

    fun createUserLeavedGroupMessage(params: MessageParams): Message {
        require(params.userId != null)
        require(params.groupId != null)
        require(!params.groupName.isNullOrBlank())
        return Message(
            id = MessageId(generateId()),
            text = "You have leaved group ${params.groupName}",
            type = MessageType.USER_LEAVED_GROUP,
            user = params.userId,
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value)
        )
    }

    fun createUserRemovedFromGroupMessage(params: MessageParams): Message {
        require(params.userId != null)
        require(params.groupId != null)
        require(!params.groupName.isNullOrBlank())
        return Message(
            id = MessageId(generateId()),
            text = "You have been removed from group ${params.groupName}",
            type = MessageType.USER_REMOVED_FROM_GROUP,
            user = params.userId,
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value)
        )
    }

    fun createMatchCreatedMessage(params: MessageParams): Message {
        require(params.userId != null)
        require(params.groupId != null)
        require(params.matchId != null)
        return Message(
            id = MessageId(generateId()),
            text = "Invented for match on ${params.start}",
            type = MessageType.MATCH_CREATED,
            user = params.userId,
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value, MATCH_ID to params.matchId.value)
        )
    }

    fun createUserDowngradedMessage(params: MessageParams): Message {
        require(params.userId != null)
        require(params.groupId != null)
        return Message(
            id = MessageId(generateId()),
            text = "You have been downgraded to player.",
            type = MessageType.USER_DOWNGRADED,
            user = params.userId,
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value)
        )
    }

    fun createUserPromotedMessage(params: MessageParams): Message {
        require(params.userId != null)
        require(params.groupId != null)
        return Message(
            id = MessageId(generateId()),
            text = "You have been promoted in to admin",
            type = MessageType.USER_PROMOTED,
            user = params.userId,
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value)
        )
    }

    fun createMatchCanceledMessage(params: MessageParams): Message {
        require(params.userId != null)
        require(params.groupId != null)
        require(params.matchId != null)
        return Message(
            id = MessageId(generateId()),
            text = "Match has been canceled",
            type = MessageType.MATCH_CANCELED,
            user = params.userId,
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value, MATCH_ID to params.matchId.value)
        )
    }

    fun createPlaygroundChangedMessage(params: MessageParams): Message {
        require(params.userId != null)
        require(params.groupId != null)
        require(!params.playground.isNullOrBlank())
        return Message(
            id = MessageId(generateId()),
            text = "Playground has been changed. New playground is ${params.playground}",
            type = MessageType.PLAYGROUND_CHANGED,
            user = params.userId,
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value)
        )
    }

    fun createPlayerAddedToCadreMessage(params: MessageParams): Message {
        require(params.userId != null)
        require(params.groupId != null)
        return Message(
            id = MessageId(generateId()),
            text = "You have been added to the cadre.",
            type = MessageType.PLAYER_ADDED_TO_CADRE,
            user = params.userId,
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value)
        )
    }

    fun createPlayerPlacedOnWaitingBenchMessage(params: MessageParams): Message {
        require(params.userId != null)
        require(params.groupId != null)
        return Message(
            id = MessageId(generateId()),
            text = "You have been placed on waiting bench.",
            type = MessageType.PLAYER_PLACED_ON_WAITING_BENCH,
            user = params.userId,
            timeStamp = LocalDateTime.now(),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value)
        )
    }
}
