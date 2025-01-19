package com.spruhs.kick_app.common

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory.disable
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class JWTSecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.cors {}.authorizeHttpRequests { auth ->
            auth.requestMatchers(HttpMethod.POST, "/api/v1/user").permitAll()
                .anyRequest().authenticated()
        }.csrf { disable() }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt(Customizer.withDefaults())
            }
        return http.build()
    }

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

    @Bean
    fun keycloak(): Keycloak {
        return KeycloakBuilder.builder()
            .serverUrl("http://localhost:8080")
            .realm("kick-app")
            .clientId("kick")
            .username("admin")
            .password("password")
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientSecret("2d80DkhYFVqr4vvGvr4Vyxnggrson6vM")
            .build()
    }
}

