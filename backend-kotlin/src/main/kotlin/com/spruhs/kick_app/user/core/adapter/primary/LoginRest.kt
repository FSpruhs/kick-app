package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.user.core.application.AuthUseCasesPort
import org.springframework.context.annotation.Profile
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
    suspend fun login(@RequestBody request: LoginRequest): AuthResponse {
        return authUseCases.login(request)
    }

    @PostMapping("/refresh/{refreshToken}")
    suspend fun refresh(@PathVariable refreshToken: String): AuthResponse {
        return authUseCases.refresh(refreshToken)
    }
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)