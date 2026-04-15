package com.spruhs.kick_app.common.helper

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class KeyedMutex<K> {
    private val mutexes = ConcurrentHashMap<K, Mutex>()

    suspend fun <T> withKeyLock(
        key: K,
        block: suspend () -> T,
    ): T {
        val mutex = mutexes.computeIfAbsent(key) { Mutex() }

        return try {
            mutex.withLock {
                block()
            }
        } finally {
            mutexes.remove(key, mutex)
        }
    }
}
