package com.spruhs.kick_app.common

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class JWTParser {
    fun getUserId(jwt: Jwt): UserId = UserId(jwt.getClaimAsString("sub"))
}