package com.spruhs.kick_app.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PreDestroy

@Configuration
class ApplicationConfiguration {

    private val log = getLogger(this::class.java)
    private val applicationCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Bean
    fun applicationScope(): CoroutineScope = applicationCoroutineScope

    @PreDestroy
    fun shutdown() {
        log.info("Shutting down application scope")
        applicationCoroutineScope.cancel()
    }
}