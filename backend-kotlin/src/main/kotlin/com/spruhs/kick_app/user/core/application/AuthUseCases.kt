package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.configs.JwtUtil
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.user.core.adapter.primary.AuthResponse
import com.spruhs.kick_app.user.core.domain.Email
import com.spruhs.kick_app.user.core.domain.Password
import com.spruhs.kick_app.user.core.domain.UserLoginPort
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@Profile("jwtSecurity")
@Service
class AuthUseCasesPort(
    private val userLoginPort: UserLoginPort,
    private val jwtUtil: JwtUtil,
) {

    suspend fun login(command: LoginCommand): AuthResponse {
        val authUser = userLoginPort.getAuthUser(command.email)
            ?: throw LoginException("Invalid email or password")
        if (!authUser.password.matches(command.password)) {
            throw LoginException("Invalid email or password")
        }

        val accessToken = jwtUtil.generateToken(authUser.userId, 10.minutes.inWholeMilliseconds)
        val refreshToken = jwtUtil.generateToken(authUser.userId, 30.days.inWholeMilliseconds)

        return AuthResponse(accessToken, refreshToken)
    }

    suspend fun refresh(refreshToken: String): AuthResponse {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw LoginException("Invalid refresh token")
        }

        val userId = jwtUtil.getUserId(refreshToken)
        val accessToken = jwtUtil.generateToken(userId, 10.minutes.inWholeMilliseconds)
        val refreshToken = jwtUtil.generateToken(userId, 30.days.inWholeMilliseconds)

        return AuthResponse(accessToken, refreshToken)
    }
}

data class LoginCommand(
    val email: Email,
    val password: String,
)

data class AuthUser(
    val email: Email,
    val userId: UserId,
    val password: Password,
)

data class LoginException(
    override val message: String,
) : RuntimeException(message)