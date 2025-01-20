package com.spruhs.kick_app.common

@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "User id must not be blank" }
    }
}

@JvmInline
value class GroupId(val value: String) {
    init {
        require(value.isNotBlank()) { "Group id must not be blank" }
    }
}