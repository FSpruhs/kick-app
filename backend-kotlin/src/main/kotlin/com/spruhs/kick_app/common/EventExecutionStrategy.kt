package com.spruhs.kick_app.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

interface EventExecutionStrategy {
    fun execute(block: suspend () -> Unit)
}

@Component
@Profile("!dev")
class AsyncEventExecutionStrategy(
    private val applicationScope: CoroutineScope
) : EventExecutionStrategy {
    override fun execute(block: suspend () -> Unit) {
        applicationScope.launch { block() }
    }
}

@Component
@Profile("dev")
class SyncEventExecutionStrategy : EventExecutionStrategy {
    override fun execute(block: suspend () -> Unit) {
        runBlocking { block() }
    }
}
