package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.user.core.application.AuthUseCasesPort
import com.spruhs.kick_app.user.core.application.LoginCommand
import com.spruhs.kick_app.user.core.application.LoginException
import com.spruhs.kick_app.user.core.domain.Email
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Profile("jwtSecurity")
@RestController
@RequestMapping("/api/v1/auth")
class LoginRestController(
    private val authUseCases: AuthUseCasesPort
) {

    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginRequest): AuthResponse =
        authUseCases.login(request.toCommand())

    @PostMapping("/refresh/{refreshToken}")
    suspend fun refresh(@PathVariable refreshToken: String): AuthResponse =
        authUseCases.refresh(refreshToken)

}

@ControllerAdvice
class LoginExceptionHandler {

    @ExceptionHandler
    fun handleLoginException(e: LoginException) =
        ResponseEntity(e.message, HttpStatus.UNAUTHORIZED)
}

private fun LoginRequest.toCommand() = LoginCommand(
    email = Email(this.email),
    password = this.password
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)