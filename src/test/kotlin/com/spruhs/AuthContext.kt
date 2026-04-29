package com.spruhs

object AuthContext {
    private val token = ThreadLocal<String?>()
    fun set(userId: String?) = token.set(userId)
    fun get(): String? = token.get()
    fun clear() = token.remove()
}
