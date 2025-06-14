package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.user.core.adapter.primary.AuthResponse
import com.spruhs.kick_app.user.core.adapter.primary.LoginRequest
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("jwtSecurity")
@Service
class AuthUseCasesPort {

    suspend fun login(request: LoginRequest): AuthResponse {
        return AuthResponse("", "")
    }

    suspend fun refresh(refreshToken: String): AuthResponse {
        return AuthResponse("", "")
    }
}