package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.user.core.application.RegisterUserCommand
import com.spruhs.kick_app.user.core.application.UserUseCases
import com.spruhs.kick_app.user.core.domain.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/user")
class UserRestController(var userUseCases: UserUseCases) {

    @GetMapping
    fun getUsers(@AuthenticationPrincipal jwt: Jwt) {
        jwt.getClaimAsString("sub")
        userUseCases.getUsers()
    }

    @PostMapping
    fun registerUser(@RequestBody request: RegisterUserRequest) {
        userUseCases.registerUser(request.toCommand())
    }
}

data class RegisterUserRequest(
    val firstName: String,
    val lastName: String,
    val nickName: String,
    val email: String,
    val password: String,
)

private fun RegisterUserRequest.toCommand() = RegisterUserCommand(
    firstName = FirstName(this.firstName),
    lastName = LastName(this.lastName),
    nickName = NickName(this.nickName),
    email = Email(this.email),
    password = Password(this.password),
)