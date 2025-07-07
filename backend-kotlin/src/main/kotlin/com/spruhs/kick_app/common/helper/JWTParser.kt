package com.spruhs.kick_app.common.helper

import com.spruhs.kick_app.common.types.UserId
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class JWTParser {
    fun getUserId(jwt: Jwt): UserId = UserId(jwt.getClaimAsString("sub"))
}