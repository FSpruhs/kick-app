package com.spruhs.kick_app.common

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.OncePerRequestFilter
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
            .cors { it.configurationSource(corsConfigurationSource) }
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
@EnableWebSecurity
class JwtSecurityConfig(
    private val corsConfigurationSource: CorsConfigurationSource
) {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtUtil: JwtUtil,
        requestLoggingFilter: RequestLoggingFilter
    ): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource) }
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.requestMatchers(HttpMethod.POST, "/api/v1/user").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                auth.anyRequest().authenticated()
            }
            .addFilterBefore(JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(requestLoggingFilter, JwtAuthFilter::class.java)
        return http.build()
    }

    @Bean
    fun jwtUtil(@Value("\${jwt.secret}") secret: String) = JwtUtil(secret)

    @Bean
    fun requestLoggingFilter() = RequestLoggingFilter()
}

@Configuration
class CorsConfiguration {
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("*")
        configuration.allowedHeaders = listOf("*")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

class JwtAuthFilter(private val jwtUtil: JwtUtil) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            if (jwtUtil.isTokenValid(token)) {
                val userId = jwtUtil.getUserId(token)
                val auth = UsernamePasswordAuthenticationToken(userId.value, null, emptyList())
                SecurityContextHolder.getContext().authentication = auth
            }
        }
        filterChain.doFilter(request, response)
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
}

class RequestLoggingFilter : OncePerRequestFilter() {

    private val log = getLogger(this::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        log.info("Incoming Request: ${request.method} ${request.requestURI}")
        filterChain.doFilter(request, response)
    }
}





