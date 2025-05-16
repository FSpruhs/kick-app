package com.spruhs.kick_app.user.core.adapter.primary

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.spruhs.kick_app.common.JWTParser
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.domain.Name
import com.spruhs.kick_app.user.core.application.UserCommandsPort
import com.spruhs.kick_app.user.core.application.UserQueryPort
import com.spruhs.kick_app.user.core.domain.Email
import com.spruhs.kick_app.user.core.domain.NickName
import com.spruhs.kick_app.user.core.domain.UserProjection
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.Date

@TestConfiguration
class TestSecurityConfig {

    @Bean
    fun jwtDecoder(): JwtDecoder = JwtDecoder { token ->
        val parts = token.split(".")
        val payloadJson = String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8)
        val claims: Map<String, Any> = jacksonObjectMapper().readValue(payloadJson)

        Jwt.withTokenValue(token)
            .header("alg", "none")
            .claims { it.putAll(claims) }
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build()
    }
}



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestSecurityConfig::class, JWTParser::class, UserRestControllerIT.TestConfig::class)
class UserRestControllerIT {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun userQueryPort(): UserQueryPort = mockk(relaxed = true)

        @Bean
        fun userCommandsPort(): UserCommandsPort = mockk(relaxed = true)
    }

    @Autowired
    lateinit var userQueryPort: UserQueryPort

    @Autowired
    lateinit var userCommandsPort: UserCommandsPort

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `should get user`() {
        val userId = "12345"
        val jwt = JWT.create()
            .withSubject(userId)
            .withClaim("sub", userId)
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
            .sign(Algorithm.none())

        coEvery { userQueryPort.getUser(UserId(userId)) } returns UserProjection(
            id = UserId(userId),
            nickName = NickName("testUser"),
            email = Email("test@testen.com"),
        )

        webTestClient.get()
            .uri("/api/v1/user/12345")
            .header("Authorization", "Bearer $jwt")
            .exchange()
            .expectStatus().isOk
    }
}
