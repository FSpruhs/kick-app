package com.spruhs.kick_app.user.core.domain

import com.spruhs.kick_app.common.DomainEvent
import com.spruhs.kick_app.common.DomainEventList
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import java.util.UUID
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

data class User(
    val id: UserId,
    val fullName: FullName,
    val nickName: NickName,
    val email: Email,
    val password: Password,
    val groups: List<GroupId>,
    override val domainEvents: List<DomainEvent> = listOf()
) : DomainEventList

fun createUser(
    fullName: FullName,
    nickName: NickName,
    email: Email,
    password: Password
): User {
    return User(UserId(UUID.randomUUID().toString()), fullName, nickName, email, password, listOf())
}

@JvmInline
value class FirstName(val value: String) {
    init {
        require(value.length in 2..20) { "First name must be between 2 and 20 characters" }
    }
}

@JvmInline
value class LastName(val value: String) {
    init {
        require(value.length in 2..20) { "Last name must be between 2 and 20 characters" }
    }
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

@JvmInline
value class Password(val value: String) {
    init {
        require(value.isNotBlank())
    }
}

data class FullName(val firstName: FirstName, val lastName: LastName)

interface UserPersistencePort {
    fun save(user: User)
    fun existsByEmail(email: Email): Boolean
    fun findById(userId: UserId): User?
    fun findAll(): List<User>
}

fun interface UserIdentityProviderPort {
    fun save(user: User)
}

data class UserNotFoundException(val userId: UserId) : RuntimeException("User not found: $userId")
data class UserWithEmailAlreadyExistsException(val email: Email) :
    RuntimeException("User with email already exists: $email")
