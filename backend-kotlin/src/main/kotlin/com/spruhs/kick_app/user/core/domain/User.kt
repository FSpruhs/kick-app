package com.spruhs.kick_app.user.core.domain

class User(
    val id: String,
    val fullName: String,
    val nickName: String,
    val email: String,
    val password: String,
    val groups: List<String>
)

interface UserPersistencePort {
    fun save(user: User)
}

interface UserIdentityProviderPort {
    fun save(user: User)
}