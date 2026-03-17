package com.spruhs.kick_app.common.types

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

@JvmInline
value class UserId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "User id must not be blank" }
    }
}

@JvmInline
value class GroupId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Group id must not be blank" }
    }
}

@JvmInline
value class MessageId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Message id must not be blank" }
    }
}

@JvmInline
value class MatchId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Match id must not be blank" }
    }
}

@JvmInline
value class UserImageId(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "User image id must not be blank" }
    }
}

@JvmInline
value class Email(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "Email is not allowed to be blank" }
        require(isValidEmail(value)) { "Invalid Email" }
    }

    companion object {
        private fun isValidEmail(email: String): Boolean =
            try {
                val emailAddr = InternetAddress(email)
                emailAddr.validate()
                true
            } catch (_: AddressException) {
                false
            }
    }
}

fun generateId(): String = UUID.randomUUID().toString()

fun String.toLocalDateTime(): LocalDateTime = LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME)

fun LocalDateTime.toISOString(): String = this.format(DateTimeFormatter.ISO_DATE_TIME)
