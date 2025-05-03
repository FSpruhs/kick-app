package com.spruhs.kick_app.user.core.domain

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.user.api.UserCreatedEvent
import com.spruhs.kick_app.user.api.UserNickNameChangedEvent
import com.spruhs.kick_app.user.core.application.ChangeUserNickNameCommand
import com.spruhs.kick_app.user.core.application.RegisterUserCommand
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

class UserAggregate(
    override val aggregateId: String
) : AggregateRoot(aggregateId, TYPE) {

    var nickName: NickName = NickName("Default")
    var email: Email = Email("default@defaults.com")

    override fun whenEvent(event: Any) {
        when (event) {
            is UserCreatedEvent -> handleUserCreatedEvent(event)
            is UserNickNameChangedEvent -> handleUserNickNameChangedEvent(event)

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

    fun createUser(command: RegisterUserCommand) {
        apply(UserCreatedEvent(aggregateId, command.email.value, command.nickName.value))
    }

    fun changeNickName(command: ChangeUserNickNameCommand) {
        apply(UserNickNameChangedEvent(aggregateId, command.nickName.value))
    }

    companion object {
        const val TYPE = "User"
    }
}

interface UserIdentityProviderPort {
    fun save(email: Email, nickName: NickName): UserId
    fun changeNickName(userId: UserId, nickName: NickName)
}

interface UserProjectionPort {
    suspend fun whenEvent(event: BaseEvent)
    suspend fun existsByEmail(email: Email): Boolean
    suspend fun getUser(userId: UserId): UserProjection?
    suspend fun findAll(exceptGroupId: GroupId?): List<UserProjection>
}

data class UserProjection (
    val id: UserId,
    val nickName: NickName,
    val email: Email,
    val groups: List<GroupProjection>,
)

data class GroupProjection(
    val id: GroupId,
    val name: String,
)

@JvmInline
value class NickName(val value: String) {
    init {
        require(value.length in 2..20) { "Nick name must be between 2 and 20 characters" }
    }
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
            } catch (ex: AddressException) {
                false
            }
        }
    }
}

data class UserNotFoundException(val userId: UserId) : RuntimeException("User not found: $userId")
data class UserWithEmailAlreadyExistsException(val email: Email) :
    RuntimeException("User with email already exists: $email")
class CreateUserIdentityProviderException(message: String) : Exception(message)
