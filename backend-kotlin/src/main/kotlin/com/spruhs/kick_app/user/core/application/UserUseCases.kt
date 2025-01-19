package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.user.core.domain.*
import org.springframework.stereotype.Service

@Service
class UserUseCases(
    val userPersistencePort: UserPersistencePort,
    val userIdentityProviderPort: UserIdentityProviderPort
) {
    fun getUser(userId: UserId): User {
        userPersistencePort.findById(userId)?.let {
            return it
        } ?: throw UserNotFoundException(userId)
    }

    fun registerUser(command: RegisterUserCommand) {
        require(
            userPersistencePort.existsByEmail(command.email).not()
        ) { UserWithEmailAlreadyExistsException(command.email) }

        User(
            FullName(command.firstName, command.lastName),
            command.nickName,
            command.email,
            command.password,
        ).apply {
            userPersistencePort.save(this)
            userIdentityProviderPort.save(this)
        }
    }
}

data class RegisterUserCommand(
    var firstName: FirstName,
    var lastName: LastName,
    var nickName: NickName,
    var email: Email,
    var password: Password,
)