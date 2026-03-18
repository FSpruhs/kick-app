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
        require(value.matches(ImageType.fileNameRegex)) {
            "User image id must match the pattern: [a-zA-Z0-9_-]+.(${ImageType.extensionPattern})"
        }
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

enum class ImageType(val mimeType: String, val extension: String) {
    PNG("image/png", "png"),
    JPEG("image/jpeg", "jpeg"),
    JPG("image/jpeg", "jpg"),
    WEBP("image/webp", "webp"),
    SVG("image/svg", "svg"),
    ;

    companion object {
        val extensionPattern: String = entries.joinToString("|") { it.extension }
        val fileNameRegex: Regex = Regex("^[a-zA-Z0-9_-]+\\.($extensionPattern)$")
        val allowedMimeTypes: Set<String> = entries.map { it.mimeType }.toSet()

        fun fromMimeType(mimeType: String): ImageType =
            entries.firstOrNull { it.mimeType == mimeType }
                ?: throw IllegalArgumentException("Unsupported content type: $mimeType")

        fun fromExtension(extension: String): ImageType? =
            entries.firstOrNull { it.extension == extension.lowercase() }
    }
}
