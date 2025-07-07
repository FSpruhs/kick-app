package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.TestHelpers.jwtWithUserId
import com.spruhs.kick_app.TestSecurityConfig
import com.spruhs.kick_app.common.helper.JWTParser
import com.spruhs.kick_app.user.core.application.AuthUseCasesPort
import com.spruhs.kick_app.user.core.application.LoginCommand
import com.spruhs.kick_app.user.core.application.LoginException
import com.spruhs.kick_app.user.core.domain.Email
import com.spruhs.kick_app.user.core.domain.UserIdentityProviderPort
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestSecurityConfig::class, JWTParser::class, LoginRestControllerTest.TestConfig::class)
@ActiveProfiles("jwtSecurity")
class LoginRestControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun authUseCases(): AuthUseCasesPort = mockk(relaxed = true)

        @Bean
        fun userIdentityProviderPort(): UserIdentityProviderPort = mockk(relaxed = true)
    }

    @Autowired
    lateinit var authUseCases: AuthUseCasesPort

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `login should authenticate user`() {
        val userId = "testUserId"
        val loginRequest = LoginRequest(
            email = "test@testen.com",
            password = "Testpassword123"
        )

        val authResponse = AuthResponse(
            accessToken = "accessToken",
            refreshToken = "refreshToken",
        )

        coEvery { authUseCases.login(LoginCommand(Email(loginRequest.email), loginRequest.password)) }.returns(authResponse)

        webTestClient.post()
            .uri("/api/v1/auth/login")
            .header("Authorization", "Bearer ${jwtWithUserId(userId)}")
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody<AuthResponse>()
            .consumeWith { response ->
                val body = response.responseBody
                assertThat(body).isNotNull
                assertThat(body?.accessToken).isEqualTo("accessToken")
                assertThat(body?.refreshToken).isEqualTo("refreshToken")
            }
    }

    @Test
    fun `login should return unauthorized for invalid credentials`() {
        val loginRequest = LoginRequest(
            email = "test@testen.com",
            password = "Testpassword123"
        )
        coEvery { authUseCases.login(any()) }
            .throws(LoginException("Invalid credentials"))

        webTestClient.post()
            .uri("/api/v1/auth/login")
            .bodyValue(loginRequest)
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody<String>()
            .consumeWith { response ->
                val body = response.responseBody
                assertThat(body).isEqualTo("Invalid credentials")
            }
    }

    @Test
    fun `refresh should return new tokens`() {
        val userId = "testUserId"
        val refreshToken = "refreshToken"
        val authResponse = AuthResponse(
            accessToken = "accessToken",
            refreshToken = "newRefreshToken",
        )
        coEvery { authUseCases.refresh(refreshToken) }.returns(authResponse)
        webTestClient.post()
            .uri("/api/v1/auth/refresh/$refreshToken")
            .header("Authorization", "Bearer ${jwtWithUserId(userId)}")
            .exchange()
            .expectStatus().isOk
            .expectBody<AuthResponse>()
            .consumeWith { response ->
                val body = response.responseBody
                assertThat(body).isNotNull
                assertThat(body?.accessToken).isEqualTo("accessToken")
                assertThat(body?.refreshToken).isEqualTo("newRefreshToken")
            }
    }
}