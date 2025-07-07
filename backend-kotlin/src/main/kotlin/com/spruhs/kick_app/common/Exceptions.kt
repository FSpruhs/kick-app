package com.spruhs.kick_app.common

data class UserNotAuthorizedException(val userId: UserId) : RuntimeException("User not authorized: $userId")
data class MessageNotFoundException(val messageId: MessageId) : RuntimeException("Message not found: $messageId")
data class UserNotFoundException(val userId: UserId) : RuntimeException("User not found: $userId")
data class PlayerNotFoundException(val userId: UserId) : RuntimeException("Player not found with id: ${userId.value}")
data class GroupNotFoundException(val groupId: GroupId) : RuntimeException("Group not found with id: ${groupId.value}")
data class MatchNotFoundException(val matchId: MatchId) : RuntimeException("Match not found with id: ${matchId.value}")