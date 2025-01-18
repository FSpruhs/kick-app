package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.user.core.domain.User
import com.spruhs.kick_app.user.core.domain.UserPersistencePort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserUseCases(val userPersistencePort: UserPersistencePort) {
    fun getUsers() {
        println("get")
    }

    fun registerUser(command: RegisterUserCommand) {
        userPersistencePort.save(
            User(
                UUID.randomUUID().toString(),
                command.firstName + " " + command.lastName,
                command.nickName,
                command.email,
                command.password,
                listOf()
        ))
    }
}

data class RegisterUserCommand(
    var firstName: String,
    var lastName: String,
    var nickName: String,
    var email: String,
    var password: String,
)