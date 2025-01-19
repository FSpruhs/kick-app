package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.user.core.application.RegisterUserCommand
import com.spruhs.kick_app.user.core.application.UserUseCases
import com.spruhs.kick_app.user.core.domain.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/user")
class UserRestController(val userUseCases: UserUseCases, val jwtParser: JWTParser) {

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String, @AuthenticationPrincipal jwt: Jwt): UserMessage {
        val userId = jwtParser.getUserId(jwt)
        if (userId != id) {
            throw UserNotAuthorizedException(UserId(userId))
        }
        return userUseCases.getUser(UserId(userId)).toMessage()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(@RequestBody request: RegisterUserRequest) {
        userUseCases.registerUser(request.toCommand())
    }
}

@ControllerAdvice
class UserExceptionHandler {

    @ExceptionHandler
    fun handleUserWithEmailAlreadyExistsException(e: UserWithEmailAlreadyExistsException) =
        ResponseEntity(e.message, HttpStatus.BAD_REQUEST)

    @ExceptionHandler
    fun handleUserNotAuthorizedException(e: UserNotAuthorizedException) =
        ResponseEntity(e.message, HttpStatus.UNAUTHORIZED)

}

data class RegisterUserRequest(
    val firstName: String,
    val lastName: String,
    val nickName: String,
    val email: String,
    val password: String,
)

data class UserMessage(
    val id: String,
    val firstName: String,
    val lastName: String,
    val nickName: String,
    val email: String,
)

private fun RegisterUserRequest.toCommand() = RegisterUserCommand(
    firstName = FirstName(this.firstName),
    lastName = LastName(this.lastName),
    nickName = NickName(this.nickName),
    email = Email(this.email),
    password = Password(this.password),
)

private fun User.toMessage() = UserMessage(
    id = this.id.value,
    firstName = this.fullName.firstName.value,
    lastName = this.fullName.lastName.value,
    nickName = this.nickName.value,
    email = this.email.value,
)