package com.spruhs.kick_app.user.core.domain

import com.spruhs.kick_app.common.es.AggregateRoot
import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.es.UnknownEventTypeException
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.UserImageId
import com.spruhs.kick_app.user.api.UserCreatedEvent
import com.spruhs.kick_app.user.api.UserImageUpdatedEvent
import com.spruhs.kick_app.user.api.UserNickNameChangedEvent
import com.spruhs.kick_app.user.core.application.AuthUser
import com.spruhs.kick_app.user.core.application.ChangeUserNickNameCommand
import com.spruhs.kick_app.user.core.application.RegisterUserCommand
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.io.InputStream
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

class UserAggregate(
    override val aggregateId: String
) : AggregateRoot(aggregateId, TYPE) {

    var nickName: NickName = NickName("Default")
    var email: Email = Email("default@defaults.com")
    var userImageId: UserImageId? = null

    override fun whenEvent(event: BaseEvent) {
        when (event) {
            is UserCreatedEvent -> handleUserCreatedEvent(event)
            is UserNickNameChangedEvent -> handleUserNickNameChangedEvent(event)
            is UserImageUpdatedEvent -> handleUserImageIdUpdatedEvent(event)

            else -> throw UnknownEventTypeException(event)
        }
    }

    private fun handleUserCreatedEvent(event: UserCreatedEvent) {
        nickName = NickName(event.nickName)
        email = Email(event.email)
    }

    private fun handleUserNickNameChangedEvent(event: UserNickNameChangedEvent) {
        nickName = NickName(event.nickName)
    }

    private fun handleUserImageIdUpdatedEvent(event: UserImageUpdatedEvent) {
        userImageId = event.imageId
    }

    fun createUser(command: RegisterUserCommand) {
        apply(UserCreatedEvent(aggregateId, command.email.value, command.nickName.value))
    }

    fun changeNickName(command: ChangeUserNickNameCommand) {
        apply(UserNickNameChangedEvent(aggregateId, command.nickName.value))
    }

    fun updateUserImage(imageId: UserImageId) {
        apply(UserImageUpdatedEvent(aggregateId, imageId))
    }

    companion object {
        const val TYPE = "User"
    }
}

interface UserIdentityProviderPort {
    suspend fun save(email: Email, nickName: NickName, password: Password? = null, userId: UserId? = null): UserId
    suspend fun changeNickName(userId: UserId, nickName: NickName)
}

interface UserLoginPort {
    suspend fun getAuthUser(email: Email): AuthUser?
}

@JvmInline
value class NickName(val value: String) {
    init {
        require(value.length in 2..20) { "Nick name must be between 2 and 20 characters" }
    }
}


@JvmInline
value class Password private constructor(val value: String) {

    companion object {
        private val encoder = BCryptPasswordEncoder()

        fun fromPlaintext(plaintext: String): Password {
            require(plaintext.length >= 8) { "Password must be at least 8 characters long" }
            require(plaintext.any { it.isDigit() }) { "Password must contain at least one digit" }
            require(plaintext.any { it.isUpperCase() }) { "Password must contain at least one uppercase letter" }
            require(plaintext.any { it.isLowerCase() }) { "Password must contain at least one lowercase letter" }

            return Password(encoder.encode(plaintext))
        }

        fun fromHash(hash: String): Password {
            return Password(hash)
        }

    }

    fun matches(plaintext: String): Boolean {
        return encoder.matches(plaintext, value)
    }
}


interface UserImagePort {
    fun save(inputStream: InputStream, contentType: String): UserImageId
}

@JvmInline
value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email is not allowed to be blank" }
        require(isValidEmail(value)) { "Invalid Email" }
    }

    companion object {
        private fun isValidEmail(email: String): Boolean {
            return try {
                val emailAddr = InternetAddress(email)
                emailAddr.validate()
                true
            } catch (_: AddressException) {
                false
            }
        }
    }
}

data class UserWithEmailAlreadyExistsException(val email: Email) :
    RuntimeException("User with email already exists: $email")

class CreateUserIdentityProviderException(message: String) : RuntimeException(message)
