package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UnknownEventTypeException
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserImageId
import com.spruhs.kick_app.group.api.*
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.user.api.UserCreatedEvent
import com.spruhs.kick_app.user.api.UserImageUpdatedEvent
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
import java.time.LocalDateTime

@Document(collection = "users")
data class UserDocument(
    @Id
    val id: String,
    var nickName: String,
    val email: String,
    var userImageId: String? = null,
    var groups: List<GroupDocument>
)

@Document
data class GroupDocument(
    val id: String,
    var name: String,
    var userRole: String,
    var userStatus: String,
    var lastMatch: LocalDateTime? = null,
)

@Service
class UserProjectionMongoAdapter(
    private val repository: UserRepository,
) : UserProjectionPort {

    override suspend fun whenEvent(event: BaseEvent) {
        when (event) {
            is UserCreatedEvent -> handleUserCreated(event)
            is UserNickNameChangedEvent -> handleUserNickNameChanged(event)
            is UserImageUpdatedEvent -> handleUserImageUpdated(event)

            is GroupNameChangedEvent -> handleGroupNameChanged(event)
            is GroupCreatedEvent -> addGroupToUser(event.userId, event.toDocument())
            is PlayerEnteredGroupEvent -> addGroupToUser(event.userId, event.toDocument())
            is PlayerRemovedEvent -> removeGroupFromUser(event.userId, event.aggregateId)
            is PlayerLeavedEvent -> removeGroupFromUser(event.userId, event.aggregateId)
            is PlayerActivatedEvent -> updateUserStatus(
                event.userId,
                GroupId(event.aggregateId),
                PlayerStatusType.ACTIVE
            )
            is PlayerDeactivatedEvent -> updateUserStatus(
                event.userId,
                GroupId(event.aggregateId),
                PlayerStatusType.INACTIVE
            )
            is PlayerPromotedEvent -> updateUserRole(event.userId, GroupId(event.aggregateId), PlayerRole.COACH)
            is PlayerDowngradedEvent -> updateUserRole(event.userId, GroupId(event.aggregateId), PlayerRole.PLAYER)

            is MatchResultEnteredEvent -> updateLastMatch(event.groupId, event.start)
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

    private suspend fun updateUserStatus(
        userId: UserId,
        groupId: GroupId,
        status: PlayerStatusType
    ) {
        val user = findUserById(userId.value)
        user.groups.find { group ->
            group.id == groupId.value
        }?.userStatus = status.name
        repository.save(user).awaitSingle()
    }

    private suspend fun updateLastMatch(
        groupId: GroupId,
        lastMatch: LocalDateTime
    ) {
        val users = repository.findByGroupsId(groupId.value).collectList().awaitSingle()
        users.forEach { user ->
            user.groups.find { it.id == groupId.value }?.lastMatch = lastMatch
        }
        repository.saveAll(users).awaitFirstOrNull()
    }

    private suspend fun updateUserRole(
        userId: UserId,
        groupId: GroupId,
        role: PlayerRole
    ) {
        val user = findUserById(userId.value)
        user.groups.find { group ->
            group.id == groupId.value
        }?.userRole = role.name
        repository.save(user).awaitSingle()
    }

    private suspend fun handleGroupNameChanged(event: GroupNameChangedEvent) {
        val users = repository.findByGroupsId(event.aggregateId).collectList().awaitSingle()
        users.forEach { group ->
            group.groups.find { it.id == event.aggregateId }?.name = event.name
        }
        repository.saveAll(users).awaitFirstOrNull()
    }

    private suspend fun addGroupToUser(
        userId: UserId,
        groupDocument: GroupDocument
    ) {
        findUserById(userId.value).let {
            it.groups += groupDocument
            repository.save(it).awaitSingle()
        }
    }

    private suspend fun removeGroupFromUser(
        userId: UserId,
        groupId: String
    ) {
        findUserById(userId.value).let {
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

    private suspend fun findUserById(userId: String): UserDocument = repository
        .findById(userId)
        .awaitFirstOrNull()
        ?: throw UserNotFoundException(UserId(userId))

    private suspend fun handleUserImageUpdated(event: UserImageUpdatedEvent) {
        findUserById(event.aggregateId).let {
            it.userImageId = event.imageId.value
            repository.save(it).awaitSingle()
        }
    }
}

@Repository
interface UserRepository : ReactiveMongoRepository<UserDocument, String> {
    fun existsByEmail(email: String): Mono<Boolean>

    fun findByGroupsIdNot(groupId: String): Flux<UserDocument>

    fun findByGroupsId(groupId: String): Flux<UserDocument>
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
    userImageId = this.userImageId?.let { UserImageId(it) },
    groups = this.groups.map {
        GroupProjection(
            id = GroupId(it.id),
            name = it.name,
            userStatus = PlayerStatusType.valueOf(it.userStatus),
            userRole = PlayerRole.valueOf(it.userRole),
            lastMatch = it.lastMatch
        )
    }
)

private fun GroupCreatedEvent.toDocument() = GroupDocument(
    id = this.aggregateId,
    name = this.name,
    userRole = this.userRole.name,
    userStatus = this.userStatus.name,
    lastMatch = null
)

private fun PlayerEnteredGroupEvent.toDocument() = GroupDocument(
    id = this.aggregateId,
    name = this.groupName,
    userRole = this.userRole.name,
    userStatus = this.userStatus.name,
    lastMatch = null
)
