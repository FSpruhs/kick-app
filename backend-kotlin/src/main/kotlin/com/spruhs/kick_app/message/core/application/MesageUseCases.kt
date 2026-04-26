package com.spruhs.kick_app.message.core.application

import com.spruhs.kick_app.common.exceptions.MessageNotFoundException
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.types.MessageId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.generateId
import com.spruhs.kick_app.group.api.GroupApi
import com.spruhs.kick_app.message.core.domain.Message
import com.spruhs.kick_app.message.core.domain.MessagePersistencePort
import com.spruhs.kick_app.message.core.domain.MessageType
import com.spruhs.kick_app.message.core.domain.messageReadBy
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime

@Service
class MessageUseCases(
    private val messagePersistencePort: MessagePersistencePort,
    private val groupApi: GroupApi,
    private val clock: Clock,
) {
    suspend fun send(
        messageType: MessageType,
        params: MessageParams,
    ) {
        createMessage(messageType, params, clock).apply {
            messagePersistencePort.save(this)
        }
    }

    suspend fun delete(
        messageType: MessageType,
        userId: UserId,
        groupId: String,
    ) {
        messagePersistencePort.deleteByTypeAndUser(messageType, userId, groupId)
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
        groupId: GroupId,
    ) {
        groupApi
            .getActivePlayers(groupId)
            .map { createMessage(messageType, params.copy(userId = it), clock) }
            .toList()
            .let { messagePersistencePort.saveAll(it) }
    }

    private suspend fun fetchMessage(messageId: MessageId): Message =
        messagePersistencePort.findById(messageId) ?: throw MessageNotFoundException(messageId)
}

data class MarkAsReadCommand(
    val messageId: MessageId,
    val userId: UserId,
)

data class MessageParams(
    val userId: UserId? = null,
    val groupId: GroupId? = null,
    val groupName: String? = null,
    val matchId: MatchId? = null,
    val start: LocalDateTime? = null,
    val playground: String? = null,
)

private fun createMessage(
    type: MessageType,
    params: MessageParams,
    clock: Clock,
): Message =
    when (type) {
        MessageType.USER_INVITED_TO_GROUP -> MessageFactory(clock).createUserInvitedToGroupMessage(params)
        MessageType.USER_LEAVED_GROUP -> MessageFactory(clock).createUserLeavedGroupMessage(params)
        MessageType.USER_REMOVED_FROM_GROUP -> MessageFactory(clock).createUserRemovedFromGroupMessage(params)
        MessageType.MATCH_CREATED -> MessageFactory(clock).createMatchCreatedMessage(params)
        MessageType.USER_DOWNGRADED -> MessageFactory(clock).createUserDowngradedMessage(params)
        MessageType.USER_PROMOTED -> MessageFactory(clock).createUserPromotedMessage(params)
        MessageType.MATCH_CANCELED -> MessageFactory(clock).createMatchCanceledMessage(params)
        MessageType.PLAYGROUND_CHANGED -> MessageFactory(clock).createPlaygroundChangedMessage(params)
        MessageType.PLAYER_ADDED_TO_CADRE -> MessageFactory(clock).createPlayerAddedToCadreMessage(params)
        MessageType.PLAYER_PLACED_ON_WAITING_BENCH -> MessageFactory(clock).createPlayerPlacedOnWaitingBenchMessage(params)
    }

private const val GROUP_ID = "groupId"
private const val MATCH_ID = "matchId"

class MessageFactory(
    private val clock: Clock,
) {
    fun createUserInvitedToGroupMessage(params: MessageParams): Message {
        requireNotNull(params.userId)
        requireNotNull(params.groupId)
        require(!params.groupName.isNullOrBlank())
        return Message(
            id = MessageId(generateId()),
            text = "You have been invited to group ${params.groupName}",
            type = MessageType.USER_INVITED_TO_GROUP,
            user = params.userId,
            timeStamp = LocalDateTime.now(clock),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value),
        )
    }

    fun createUserLeavedGroupMessage(params: MessageParams): Message {
        requireNotNull(params.userId)
        requireNotNull(params.groupId)
        require(!params.groupName.isNullOrBlank())
        return Message(
            id = MessageId(generateId()),
            text = "You have leaved group ${params.groupName}",
            type = MessageType.USER_LEAVED_GROUP,
            user = params.userId,
            timeStamp = LocalDateTime.now(clock),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value),
        )
    }

    fun createUserRemovedFromGroupMessage(params: MessageParams): Message {
        requireNotNull(params.userId)
        requireNotNull(params.groupId)
        require(!params.groupName.isNullOrBlank())
        return Message(
            id = MessageId(generateId()),
            text = "You have been removed from group ${params.groupName}",
            type = MessageType.USER_REMOVED_FROM_GROUP,
            user = params.userId,
            timeStamp = LocalDateTime.now(clock),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value),
        )
    }

    fun createMatchCreatedMessage(params: MessageParams): Message {
        requireNotNull(params.userId)
        requireNotNull(params.groupId)
        requireNotNull(params.matchId)
        return Message(
            id = MessageId(generateId()),
            text = "Invented for match on ${params.start}",
            type = MessageType.MATCH_CREATED,
            user = params.userId,
            timeStamp = LocalDateTime.now(clock),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value, MATCH_ID to params.matchId.value),
        )
    }

    fun createUserDowngradedMessage(params: MessageParams): Message {
        requireNotNull(params.userId)
        requireNotNull(params.groupId)
        return Message(
            id = MessageId(generateId()),
            text = "You have been downgraded to player.",
            type = MessageType.USER_DOWNGRADED,
            user = params.userId,
            timeStamp = LocalDateTime.now(clock),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value),
        )
    }

    fun createUserPromotedMessage(params: MessageParams): Message {
        requireNotNull(params.userId)
        requireNotNull(params.groupId)
        return Message(
            id = MessageId(generateId()),
            text = "You have been promoted in to admin",
            type = MessageType.USER_PROMOTED,
            user = params.userId,
            timeStamp = LocalDateTime.now(clock),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value),
        )
    }

    fun createMatchCanceledMessage(params: MessageParams): Message {
        requireNotNull(params.userId)
        requireNotNull(params.groupId)
        requireNotNull(params.matchId)
        return Message(
            id = MessageId(generateId()),
            text = "Match has been canceled",
            type = MessageType.MATCH_CANCELED,
            user = params.userId,
            timeStamp = LocalDateTime.now(clock),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value, MATCH_ID to params.matchId.value),
        )
    }

    fun createPlaygroundChangedMessage(params: MessageParams): Message {
        requireNotNull(params.userId)
        requireNotNull(params.groupId)
        require(!params.playground.isNullOrBlank())
        return Message(
            id = MessageId(generateId()),
            text = "Playground has been changed. New playground is ${params.playground}",
            type = MessageType.PLAYGROUND_CHANGED,
            user = params.userId,
            timeStamp = LocalDateTime.now(clock),
            isRead = false,
            variables = mapOf(GROUP_ID to params.groupId.value),
        )
    }

    fun createPlayerAddedToCadreMessage(params: MessageParams): Message {
        requireNotNull(params.userId)
        requireNotNull(params.matchId)
        return Message(
            id = MessageId(generateId()),
            text = "You have been added to the cadre.",
            type = MessageType.PLAYER_ADDED_TO_CADRE,
            user = params.userId,
            timeStamp = LocalDateTime.now(clock),
            isRead = false,
            variables = mapOf(MATCH_ID to params.matchId.value),
        )
    }

    fun createPlayerPlacedOnWaitingBenchMessage(params: MessageParams): Message {
        requireNotNull(params.userId)
        requireNotNull(params.matchId)
        return Message(
            id = MessageId(generateId()),
            text = "You have been placed on waiting bench.",
            type = MessageType.PLAYER_PLACED_ON_WAITING_BENCH,
            user = params.userId,
            timeStamp = LocalDateTime.now(clock),
            isRead = false,
            variables = mapOf(MATCH_ID to params.matchId.value),
        )
    }
}
