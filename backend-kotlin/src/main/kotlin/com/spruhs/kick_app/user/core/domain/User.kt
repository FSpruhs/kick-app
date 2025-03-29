package com.spruhs.kick_app.user.core.domain

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.user.core.application.ChangeUserNickNameCommand
import com.spruhs.kick_app.user.core.application.RegisterUserCommand
import org.springframework.stereotype.Component
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

class UserAggregate(
    override val aggregateId: String
) : AggregateRoot(aggregateId, TYPE) {

    var nickName: NickName = NickName("Default")
    var email: Email = Email("default@defaults.com")

    override fun whenEvent(event: Any) {
        when (event) {
            is UserCreatedEvent -> {
                nickName = NickName(event.nickName)
                email = Email(event.email)
            }

            is UserNickNameChangedEvent -> {
                nickName = NickName(event.nickName)
            }

            else -> throw UnknownEventTypeException(event)
        }
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

@Component
class UserEventSerializer : Serializer {
    override fun serialize(event: Any, aggregate: AggregateRoot): Event {
        val data = EventSourcingUtils.writeValueAsBytes(event)

        return when (event) {
            is UserCreatedEvent -> Event(
                aggregate,
                UserEvents.USER_CREATED_V1.name,
                data,
                event.metadata
            )

            is UserNickNameChangedEvent -> Event(
                aggregate,
                UserEvents.USER_NICKNAME_CHANGED_V1.name,
                data,
                event.metadata
            )

            else -> throw UnknownEventTypeException(event)
        }
    }

    override fun deserialize(event: Event): Any {
        return when (event.type) {
            UserEvents.USER_CREATED_V1.name -> EventSourcingUtils.readValue(
                event.data, UserCreatedEvent::class.java
            )

            UserEvents.USER_NICKNAME_CHANGED_V1.name -> EventSourcingUtils.readValue(
                event.data, UserNickNameChangedEvent::class.java
            )

            else -> throw UnknownEventTypeException(event)
        }
    }

}

data class UserProjection (
    val id: UserId,
    val nickName: NickName,
    val email: Email,
    val groups: List<GroupId>,
)

data class UserCreatedEvent(
    override val aggregateId: String,
    val email: String,
    val nickName: String,
) : BaseEvent(aggregateId)

data class UserNickNameChangedEvent(
    override val aggregateId: String,
    val nickName: String
) : BaseEvent(aggregateId)

enum class UserEvents {
    USER_CREATED_V1,
    USER_NICKNAME_CHANGED_V1,
}

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
