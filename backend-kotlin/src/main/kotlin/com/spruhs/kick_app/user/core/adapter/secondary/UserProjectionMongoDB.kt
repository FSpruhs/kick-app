package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UnknownEventTypeException
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.api.*
import com.spruhs.kick_app.user.api.UserCreatedEvent
import com.spruhs.kick_app.user.api.UserNickNameChangedEvent
import com.spruhs.kick_app.user.core.domain.*
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
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
    var groups: List<GroupDocument>
)

@Document
data class GroupDocument(
    val id: String,
    var name: String,
)

@Service
class UserProjectionMongoAdapter(
    private val repository: UserRepository,
) : UserProjectionPort {

    override suspend fun whenEvent(event: BaseEvent) {
        when (event) {
            is UserCreatedEvent -> handleUserCreated(event)
            is UserNickNameChangedEvent -> handleUserNickNameChanged(event)
            is GroupNameChangedEvent -> handleGroupNameChanged(event)
            is GroupCreatedEvent -> addGroupToUser(event.userId, event.aggregateId, event.name)
            is PlayerEnteredGroupEvent -> addGroupToUser(event.userId, event.aggregateId, event.name)
            is PlayerRemovedEvent -> removeGroupFromUser(event.userId, event.aggregateId)
            is PlayerLeavedEvent -> removeGroupFromUser(event.userId, event.aggregateId)
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
            repository.findByGroupsIdNot(exceptGroupId.value)
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

    private suspend fun handleGroupNameChanged(event: GroupNameChangedEvent) {
        val users = repository.findByGroupsName(event.aggregateId).collectList().awaitSingle()
        users.forEach { group ->
            group.groups.find { it.id == event.aggregateId }?.name = event.name
        }
        repository.saveAll(users).awaitFirstOrNull()
    }

    private suspend fun addGroupToUser(userId: String, groupId: String, groupName: String) {
        findUserById(userId).let {
            it.groups += GroupDocument(groupId, groupName)
            repository.save(it).awaitSingle()
        }
    }

    private suspend fun removeGroupFromUser(userId: String, groupId: String) {
        findUserById(userId).let {
            it.groups = it.groups.filter { group -> group.id != groupId }
            repository.save(it).awaitSingle()
        }
    }

    private suspend fun handleUserNickNameChanged(event: UserNickNameChangedEvent) {
        findUserById(event.aggregateId).let {
            it.nickName = event.nickName
            repository.save(it).awaitSingle()
        }
    }

    private suspend fun handleUserCreated(event: UserCreatedEvent) {
        repository.save(event.toDocument()).awaitFirstOrNull()
    }

    private suspend fun findUserById(userId: String): UserDocument {
        return repository.findById(userId).awaitFirstOrNull()?: throw UserNotFoundException(UserId(userId))
    }
}

@Repository
interface UserRepository : ReactiveMongoRepository<UserDocument, String> {
    fun existsByEmail(email: String): Mono<Boolean>

    fun findByGroupsIdNot(groupId: String): Flux<UserDocument>

    fun findByGroupsName(groupName: String): Flux<UserDocument>
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
    groups = this.groups.map { GroupProjection(GroupId(it.id), it.name) }
)
