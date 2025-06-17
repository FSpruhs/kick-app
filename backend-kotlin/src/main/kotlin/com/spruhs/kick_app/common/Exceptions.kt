package com.spruhs.kick_app.common

data class UserNotAuthorizedException(val userId: UserId) : RuntimeException("User not authorized: $userId")
data class MessageNotFoundException(val messageId: MessageId) : RuntimeException("Message not found: $messageId")
data class UserNotFoundException(val userId: UserId) : RuntimeException("User not found: $userId")