package com.spruhs.kick_app

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.Base64
import java.util.Date

@TestConfiguration
class TestSecurityConfig {

    @Bean
    fun reactiveJwtDecoder(): ReactiveJwtDecoder = ReactiveJwtDecoder { token ->
        val parts = token.split(".")
        val payloadJson = String(Base64.getDecoder().decode(parts[1]), Charsets.UTF_8)
        val claims: Map<String, Any> = jacksonObjectMapper().readValue(payloadJson)

        Mono.just(
            Jwt.withTokenValue(token)
                .header("alg", "none")
                .claims { it.putAll(claims) }
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()
        )
    }
}

object TestHelpers {
    fun jwtWithUserId(userId: String): String {
        return JWT.create()
            .withSubject(userId)
            .withIssuedAt(Date.from(Instant.now()))
            .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
            .sign(Algorithm.none())
    }
}