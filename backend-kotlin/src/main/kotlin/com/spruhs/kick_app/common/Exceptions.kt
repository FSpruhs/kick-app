package com.spruhs.kick_app.common

data class UserNotAuthorizedException(val userId: UserId) : RuntimeException("User not authorized: $userId")