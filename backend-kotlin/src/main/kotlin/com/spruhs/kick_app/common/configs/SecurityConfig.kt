package com.spruhs.kick_app.common.configs

import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.helper.getLogger
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.Date
import javax.crypto.SecretKey

@Profile("oauth2")
@Configuration
class OAuth2SecurityConfig(
    private val corsConfigurationSource: CorsConfigurationSource
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            //.cors { it.configurationSource(corsConfigurationSource) }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.POST, "/api/v1/user").permitAll()
                auth.anyRequest().authenticated()
            }
            .csrf { it.disable() }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
            }
        return http.build()
    }

    @Bean
    fun keycloak(): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl("http://localhost:8080")
            .realm("kick-app")
            .clientId("kick")
            .username("admin")
            .password("password")
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientSecret("**********")
            .build()
    }
}

@Profile("jwtSecurity")
@Configuration
class JwtSecurityConfig(
    private val corsConfigurationSource: CorsConfigurationSource
) {
    @Bean
    fun securityFilterChain(
        http: ServerHttpSecurity,
        jwtUtil: JwtUtil,
    ): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource) }
            .authorizeExchange { exchanges ->
                exchanges.pathMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/v1/user").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                    .anyExchange().authenticated()
            }

            .addFilterAt(JwtAuthenticationWebFilter(jwtUtil), SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }

    @Bean
    fun jwtUtil(@Value("\${jwt.secret}") secret: String) = JwtUtil(secret)

}

@Configuration
class CorsConfiguration {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOrigins = listOf("*")
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("*")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}

class JwtAuthenticationWebFilter(
    private val jwtUtil: JwtUtil
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val token = extractToken(exchange.request)
        return if (token != null && jwtUtil.isTokenValid(token)) {
            val jwt = jwtUtil.toJwt(token)
            val auth = UsernamePasswordAuthenticationToken(jwt, null, listOf(SimpleGrantedAuthority("ROLE_USER")))
            val context = SecurityContextImpl(auth)

            chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)))
        } else {
            chain.filter(exchange)
        }
    }

    private fun extractToken(request: ServerHttpRequest): String? {
        val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return null
        return if (authHeader.startsWith("Bearer ")) authHeader.substring(7) else null
    }
}

class JwtUtil(secret: String) {

    private val log = getLogger(this::class.java)
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(userId: UserId, expirationMillis: Long): String {
        return Jwts.builder()
            .setSubject(userId.value)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expirationMillis))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getUserId(token: String): UserId =
        UserId(Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body.subject)

    fun isTokenValid(token: String): Boolean = try {
        Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
        true
    } catch (e: Exception) {
        log.error(e.message)
        false
    }

    fun toJwt(token: String): Jwt {
        val claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
        return Jwt.withTokenValue(token)
            .header("alg", "HS256")
            .header("typ", "JWT")
            .subject(claims.subject)
            .issuedAt(claims.issuedAt.toInstant())
            .expiresAt(claims.expiration.toInstant())
            .build()
    }
}





