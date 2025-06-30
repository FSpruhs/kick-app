package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UnknownEventTypeException
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserImageId
import com.spruhs.kick_app.common.UserNotFoundException
import com.spruhs.kick_app.group.api.GroupCreatedEvent
import com.spruhs.kick_app.group.api.GroupNameChangedEvent
import com.spruhs.kick_app.group.api.PlayerActivatedEvent
import com.spruhs.kick_app.group.api.PlayerDeactivatedEvent
import com.spruhs.kick_app.group.api.PlayerDowngradedEvent
import com.spruhs.kick_app.group.api.PlayerEnteredGroupEvent
import com.spruhs.kick_app.group.api.PlayerLeavedEvent
import com.spruhs.kick_app.group.api.PlayerPromotedEvent
import com.spruhs.kick_app.group.api.PlayerRemovedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.user.api.UserCreatedEvent
import com.spruhs.kick_app.user.api.UserImageUpdatedEvent
import com.spruhs.kick_app.user.api.UserNickNameChangedEvent
import com.spruhs.kick_app.view.api.UserApi
import com.spruhs.kick_app.view.api.UserData
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserService(
    private val repository: UserProjectionRepository
) {
    suspend fun whenEvent(event: BaseEvent) {
        when (event) {
            is UserCreatedEvent -> handleUserCreated(event)
            is UserNickNameChangedEvent -> handleNickNameChanged(event)
            is UserImageUpdatedEvent -> handleUserImageUpdated(event)

            is MatchResultEnteredEvent -> updateLastMatch(event.groupId, event.start)

            is GroupNameChangedEvent -> handleGroupNameChanged(event)
            is GroupCreatedEvent -> addGroupToUser(event.userId, event.toProjection())
            is PlayerEnteredGroupEvent -> addGroupToUser(event.userId, event.toProjection())
            is PlayerRemovedEvent -> removeGroupFromUser(event.userId, GroupId(event.aggregateId))
            is PlayerLeavedEvent -> removeGroupFromUser(event.userId, GroupId(event.aggregateId))
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
            else -> throw UnknownEventTypeException(event)
        }
    }

    private suspend fun handleUserCreated(event: UserCreatedEvent) {
        repository.save(event.toProjection())
    }

    private suspend fun handleNickNameChanged(event: UserNickNameChangedEvent) {
        fetchUser(UserId(event.aggregateId)).also {
            repository.save(it.copy(nickName = event.nickName))
        }
    }

    private suspend fun handleUserImageUpdated(event: UserImageUpdatedEvent) {
        fetchUser(UserId(event.aggregateId)).also {
            repository.save(it.copy(userImageId = event.imageId))
        }
    }

    private suspend fun fetchUser(userId: UserId): UserProjection =
        repository.getUser(userId) ?: throw UserNotFoundException(userId)

    private suspend fun updateLastMatch(
        groupId: GroupId,
        lastMatch: LocalDateTime
    ) {
        repository.findByGroupId(groupId).forEach { user ->
            updatePlayerLastMatch(user, groupId, lastMatch)
        }
    }

    private suspend fun updatePlayerLastMatch(
        user: UserProjection,
        groupId: GroupId,
        lastMatch: LocalDateTime
    ) {
        fetchUserGroup(user, groupId)?.let {
            if (it.lastMatch?.isBefore(lastMatch) ?: true) {
                it.lastMatch = lastMatch
                repository.save(user)
            }
        }
    }

    private suspend fun updateUserStatus(
        userId: UserId,
        groupId: GroupId,
        status: PlayerStatusType
    ) {
        fetchUser(userId).also {
            fetchUserGroup(it, groupId)?.userStatus = status
            repository.save(it)
        }
    }

    private suspend fun fetchUserGroup(user: UserProjection, groupId: GroupId): UserGroupProjection? =
        user.groups.find { it.id == groupId }

    private suspend fun updateUserRole(
        userId: UserId,
        groupId: GroupId,
        role: PlayerRole
    ) {
        fetchUser(userId).also {
            fetchUserGroup(it, groupId)?.userRole = role
            repository.save(it)
        }
    }

    private suspend fun handleGroupNameChanged(event: GroupNameChangedEvent) {
        val groupId = GroupId(event.aggregateId)
        repository.findByGroupId(groupId).map { user ->
            fetchUserGroup(user, groupId)?.name = event.name
            repository.save(user)
        }
    }

    private suspend fun addGroupToUser(
        userId: UserId,
        userGroupProjection: UserGroupProjection
    ) {
        fetchUser(userId).let {
            it.groups += userGroupProjection
            repository.save(it)
        }
    }

    private suspend fun removeGroupFromUser(
        userId: UserId,
        groupId: GroupId
    ) {
        fetchUser(userId).let {
            it.groups = it.groups.filter { group -> group.id != groupId }
            repository.save(it)
        }
    }

    suspend fun getUser(userId: UserId): UserProjection =
        repository.getUser(userId) ?: throw UserNotFoundException(userId)
}

@Service
class UserApiService(
    private val repository: UserProjectionRepository
) : UserApi {
    override suspend fun findUserById(userId: UserId): UserData =
        repository.getUser(userId)?.toData() ?: throw UserNotFoundException(userId)

    override suspend fun existsByEmail(email: String): Boolean = repository.existsByEmail(email)
}

interface UserProjectionRepository {
    suspend fun existsByEmail(email: String): Boolean
    suspend fun getUser(userId: UserId): UserProjection?
    suspend fun save(userProjection: UserProjection)
    suspend fun saveAll(userProjection: List<UserProjection>)
    suspend fun findByGroupId(groupId: GroupId): List<UserProjection>
}

data class UserProjection(
    val id: UserId,
    val nickName: String,
    val email: String,
    val userImageId: UserImageId? = null,
    var groups: List<UserGroupProjection> = emptyList(),
)

data class UserGroupProjection(
    val id: GroupId,
    var name: String,
    var userStatus: PlayerStatusType,
    var userRole: PlayerRole,
    var lastMatch: LocalDateTime? = null,
)

private fun UserProjection.toData() = UserData(
    id = this.id,
    nickName = this.nickName,
    email = this.email
)

private fun GroupCreatedEvent.toProjection() = UserGroupProjection(
    id = GroupId(this.aggregateId),
    name = this.name,
    userRole = this.userRole,
    userStatus = this.userStatus,
)

private fun PlayerEnteredGroupEvent.toProjection() = UserGroupProjection(
    id = GroupId(this.aggregateId),
    name = this.groupName,
    userRole = this.userRole,
    userStatus = this.userStatus,
)

private fun UserCreatedEvent.toProjection() =
    UserProjection(
        id = UserId(this.aggregateId),
        nickName = this.nickName,
        email = this.email,
        groups = emptyList()
    )