package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UnknownEventTypeException
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
    var nickName: String,
    val email: String,
    var groups: List<String>
)

@Service
class UserProjectionMongoAdapter(
    private val repository: UserRepository,
) : UserProjectionPort {

    override suspend fun whenEvent(event: BaseEvent) {
        when (event) {
            is UserCreatedEvent -> handleUserCreated(event)
            is UserNickNameChangedEvent -> handleUserNickNameChanged(event)
            else -> {
                throw UnknownEventTypeException(event)
            }
        }
    }

    override suspend fun getUser(userId: UserId): UserProjection? =
        repository.findById(userId.value)
            .awaitFirstOrNull()
            ?.toProjection()

    override suspend fun findAll(exceptGroupId: GroupId?): List<UserProjection> {
        return if (exceptGroupId != null) {
            repository.findByGroupNotContaining(exceptGroupId.value)
                .collectList()
                .awaitSingle()
                .map { it.toProjection() }
        } else {
            repository.findAll()
                .collectList()
                .awaitSingle()
                .map { it.toProjection() }
        }
    }

    override suspend fun existsByEmail(email: Email): Boolean {
        return repository.existsByEmail(email.value).awaitSingle()
    }

    private suspend fun handleUserNickNameChanged(event: UserNickNameChangedEvent) {
        repository.findById(event.aggregateId).awaitFirstOrNull()?.let {
            it.nickName = event.nickName
            repository.save(it)
        } ?: throw UserNotFoundException(UserId(event.aggregateId))
    }

    private suspend fun handleUserCreated(event: UserCreatedEvent) {
        repository.save(event.toDocument()).awaitFirstOrNull()
    }
}

@Repository
interface UserRepository : ReactiveMongoRepository<UserDocument, String> {
    fun existsByEmail(email: String): Mono<Boolean>

    @Query("{ 'groups': { \$nin: [?0] } }")
    fun findByGroupNotContaining(group: String): Flux<UserDocument>
}

private fun UserCreatedEvent.toDocument() = UserDocument(
    id = this.aggregateId,
    nickName = this.nickName,
    email = this.email,
    groups = emptyList()
)

private fun UserDocument.toProjection() = UserProjection(
    id = UserId(this.id),
    nickName = NickName(this.nickName),
    email = Email(this.email),
    groups = this.groups.map { GroupId(it) }
)
