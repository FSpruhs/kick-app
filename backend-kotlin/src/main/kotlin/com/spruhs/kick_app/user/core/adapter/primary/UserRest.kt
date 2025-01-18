package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.user.core.application.RegisterUserCommand
import com.spruhs.kick_app.user.core.application.UserUseCases
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/user")
class UserRestController(var userUseCases: UserUseCases) {

    @GetMapping
    fun getUsers() {
        userUseCases.getUsers()
    }

    @PostMapping
    fun registerUser(@RequestBody request: RegisterUserRequest) {
        userUseCases.registerUser(
            RegisterUserCommand(
                request.firstName,
                request.lastName,
                request.nickName,
                request.email,
                request.password
            )
        )
    }
}

data class RegisterUserRequest(
    val firstName: String,
    val lastName: String,
    val nickName: String,
    val email: String,
    val password: String,
)