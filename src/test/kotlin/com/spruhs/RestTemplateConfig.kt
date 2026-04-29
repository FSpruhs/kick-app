package com.spruhs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate().apply {
        // Keine Exception bei 4xx/5xx – Statuscode wird in den Steps geprüft
        errorHandler = object : ResponseErrorHandler {
            override fun hasError(response: ClientHttpResponse) = false
        }
    }
}
