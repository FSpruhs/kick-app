package com.spruhs

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date

object JwtTokenFactory {

    private const val secret = "test-secret-key-for-dev-environment-only-32b"
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun createToken(userId: String): String = Jwts.builder()
        .subject(userId)
        .issuedAt(Date())
        .expiration(Date(System.currentTimeMillis() + 3_600_000))
        .signWith(key)
        .compact()
}
