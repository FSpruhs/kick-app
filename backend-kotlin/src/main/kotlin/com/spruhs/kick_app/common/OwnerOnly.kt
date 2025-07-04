package com.spruhs.kick_app.common

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OwnerOnly(
    val pathParam: String = "userId",
    val jwtParam: String = "jwt"
)


@Aspect
@Component
class OwnerOnlyAspect(
    private val jwtParser: JWTParser
) {

    @Around("@annotation(OwnerOnly)")
    fun checkOwnership(joinPoint: ProceedingJoinPoint, ownerOnly: OwnerOnly): Any? {
        val method = (joinPoint.signature as MethodSignature).method
        val args = joinPoint.args

        val expectedUserIdParam = ownerOnly.pathParam
        val expectedJwtParam = ownerOnly.jwtParam

        var userId: String? = null
        var jwt: Jwt? = null

        method.parameters.forEachIndexed { idx, param ->
            val name = param.name
            val arg = args[idx]

            if (name == expectedUserIdParam && arg is String) {
                userId = arg
            }

            if (name == expectedJwtParam && arg is Jwt) {
                jwt = arg
            }

            if (jwt == null && arg is Jwt) {
                jwt = arg
            }
        }

        if (userId == null) {
            throw IllegalStateException("Could not find userId in method arguments.")
        }

        if (jwt == null) {
            throw IllegalStateException("Could not find Jwt in method arguments.")
        }

        val jwtUserId = jwtParser.getUserId(jwt).value

        if (jwtUserId != userId) {
            throw UserNotAuthorizedException(UserId(userId))
        }

        return joinPoint.proceed()
    }
}

