package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.domain.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Document(collection = "users")
data class UserDocument(
    @Id
    val id: String,
    val firstName: String,
    val lastName: String,
    val nickName: String,
    val email: String,
    val groups: List<String>
)

@Service
class UserPersistenceAdapter(private val repository: UserRepository) : UserPersistencePort {
    override suspend fun save(user: User) {
        repository.save(user.toDocument()).awaitFirstOrNull()
    }

    override suspend fun existsByEmail(email: Email): Boolean {
        return repository.existsByEmail(email.value).awaitSingle()
    }

    override suspend fun findById(userId: UserId): User? {
        return repository.findById(userId.value).awaitFirstOrNull()?.toDomain()
    }

    override suspend fun findByIds(userIds: List<UserId>): List<User> {
        return repository.findAllById(userIds.map { it.value }).collectList().awaitSingle().map { it.toDomain() }
    }

    override suspend fun findAll(exceptGroupId: GroupId?): List<User> {
        return if (exceptGroupId != null) {
            repository.findByGroupNotContaining(exceptGroupId.value).collectList().awaitSingle().map { it.toDomain() }
        } else {
            repository.findAll().collectList().awaitSingle().map { it.toDomain() }
        }
    }
}

@Repository
interface UserRepository : ReactiveMongoRepository<UserDocument, String> {
    fun existsByEmail(email: String): Mono<Boolean>

    @Query("{ 'groups': { \$nin: [?0] } }")
    fun findByGroupNotContaining(group: String): Flux<UserDocument>
}

private fun User.toDocument() = UserDocument(
    id = id.value,
    firstName = fullName.firstName.value,
    lastName = fullName.lastName.value,
    nickName = nickName.value,
    email = email.value,
    groups = groups.map { it.value }
)

private fun UserDocument.toDomain() = User(
    UserId(id),
    FullName(FirstName(firstName), LastName(lastName)),
    NickName(nickName),
    Email(email),
    groups.map { GroupId(it) }
)