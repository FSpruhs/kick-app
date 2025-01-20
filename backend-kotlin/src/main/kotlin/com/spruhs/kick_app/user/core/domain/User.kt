package com.spruhs.kick_app.user.core.domain

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import java.util.UUID
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

class User(
    private val _id: UserId,
    private val _fullName: FullName,
    private val _nickName: NickName,
    private val _email: Email,
    private val _password: Password,
    private val _groups: List<GroupId>
) {
    constructor(
        fullName: FullName,
        nickName: NickName,
        email: Email,
        password: Password
    ) : this(UserId(UUID.randomUUID().toString()), fullName, nickName, email, password, listOf())

    val id: UserId
        get() = _id

    val fullName: FullName
        get() = _fullName

    val nickName: NickName
        get() = _nickName

    val email: Email
        get() = _email

    val password: Password
        get() = _password

    val groups: List<GroupId>
        get() = _groups
}

@JvmInline
value class FirstName(val value: String) {
    init {
        require(value.length in 2..20)
    }
}

@JvmInline
value class LastName(val value: String) {
    init {
        require(value.length in 2..20)
    }
}

@JvmInline
value class NickName(val value: String) {
    init {
        require(value.length in 2..20)
    }
}

@JvmInline
value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email darf nicht leer sein" }
        require(isValidEmail(value)) { "Ungültige E-Mail-Adresse" }
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
}

interface UserIdentityProviderPort {
    fun save(user: User)
}

data class UserNotFoundException(val userId: UserId) : RuntimeException("User not found: $userId")
data class UserWithEmailAlreadyExistsException(val email: Email) : RuntimeException("User with email already exists: $email")
