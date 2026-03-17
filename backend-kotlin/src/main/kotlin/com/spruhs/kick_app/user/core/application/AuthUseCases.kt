package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.configs.JwtUtil
import com.spruhs.kick_app.common.types.Email
import com.spruhs.kick_app.user.core.domain.AuthUser
import com.spruhs.kick_app.user.core.domain.UserLoginPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@Profile("jwtSecurity")
@Service
class AuthUseCasesPort(
    private val userLoginPort: UserLoginPort,
    private val jwtUtil: JwtUtil,
    @Value("\${jwt.expiration.accessInMinuets}") private val accessExpiration: Long = 10,
    @Value("\${jwt.expiration.refreshInDays}") private val refreshExpiration: Long = 30,
) {
    suspend fun login(command: LoginCommand): AuthTokens {
        val authUser = fetchAuthUser(command.email)
        if (!authUser.password.matches(command.password)) {
            throw LoginException("Invalid email or password")
        }

        val accessToken =
            jwtUtil.generateToken(
                authUser.userId,
                accessExpiration.minutes.inWholeMilliseconds,
            )
        val refreshToken =
            jwtUtil.generateToken(
                authUser.userId,
                refreshExpiration.days.inWholeMilliseconds,
            )

        return AuthTokens(accessToken, refreshToken)
    }

    private suspend fun fetchAuthUser(email: Email): AuthUser =
        userLoginPort.getAuthUser(email)
            ?: throw LoginException("Invalid email or password")

    suspend fun refresh(refreshToken: String): AuthTokens {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw LoginException("Invalid refresh token")
        }

        val userId = jwtUtil.getUserId(refreshToken)
        val accessToken = jwtUtil.generateToken(userId, accessExpiration.minutes.inWholeMilliseconds)
        val refreshToken = jwtUtil.generateToken(userId, refreshExpiration.days.inWholeMilliseconds)

        return AuthTokens(accessToken, refreshToken)
    }
}

data class LoginCommand(
    val email: Email,
    val password: String,
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)

data class LoginException(
    override val message: String,
) : RuntimeException(message)
