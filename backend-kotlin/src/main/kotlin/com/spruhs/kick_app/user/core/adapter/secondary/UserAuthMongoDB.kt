package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.generateId
import com.spruhs.kick_app.common.helper.getLogger
import com.spruhs.kick_app.user.core.application.AuthUser
import com.spruhs.kick_app.user.core.domain.Email
import com.spruhs.kick_app.user.core.domain.NickName
import com.spruhs.kick_app.user.core.domain.Password
import com.spruhs.kick_app.user.core.domain.UserIdentityProviderPort
import com.spruhs.kick_app.user.core.domain.UserLoginPort
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Profile("jwtSecurity")
@Service
class UserAuthMongoDBAdapter(private val repository: UserAuthRepository) : UserIdentityProviderPort, UserLoginPort {

    private val log = getLogger(this::class.java)

    override suspend fun save(
        email: Email,
        nickName: NickName,
        password: Password?,
        userId: UserId?
    ): UserId {
        require(password != null) { "Password required" }
        val newId = userId?.value ?: generateId()
        repository.save(UserAuthDocument(
            userId = newId,
            email = email.value,
            passwordHash = password.value
        )).awaitFirstOrNull()
        return UserId(newId)
    }

    override suspend fun changeNickName(
        userId: UserId,
        nickName: NickName
    ) {
        log.info("Changing nickname for user {}", nickName)
    }

    override suspend fun getAuthUser(email: Email): AuthUser? {
        return repository.findByEmail(email.value)
            .awaitFirstOrNull()
            ?.let { AuthUser(
                email = Email(it.email),
                userId = UserId(it.userId),
                password = Password.fromHash(it.passwordHash)
            ) }
    }

}

@Document(collection = "usersAuth")
data class UserAuthDocument(
    val userId: String,
    val email: String,
    val passwordHash: String,
)

@Profile("jwtSecurity")
@Repository
interface UserAuthRepository : ReactiveMongoRepository<UserAuthDocument, String> {
    fun findByEmail(email: String): Flux<UserAuthDocument>
}