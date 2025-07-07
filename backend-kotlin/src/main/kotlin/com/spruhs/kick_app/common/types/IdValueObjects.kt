package com.spruhs.kick_app.common.types

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

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

@JvmInline
value class MessageId(val value: String) {
    init {
        require(value.isNotBlank()) { "Message id must not be blank" }
    }
}

@JvmInline
value class MatchId(val value: String) {
    init {
        require(value.isNotBlank()) { "Match id must not be blank" }
    }
}

@JvmInline
value class UserImageId(val value: String) {
    init {
        require(value.isNotBlank()) { "User image id must not be blank" }
    }
}

fun generateId(): String {
    return UUID.randomUUID().toString()
}

fun String.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)
}

fun LocalDateTime.toISOString(): String {
    return this.format(DateTimeFormatter.ISO_DATE_TIME)
}
