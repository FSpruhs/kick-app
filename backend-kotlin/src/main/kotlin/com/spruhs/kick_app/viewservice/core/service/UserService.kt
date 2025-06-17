package com.spruhs.kick_app.viewservice.core.service

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UnknownEventTypeException
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserImageId
import com.spruhs.kick_app.common.UserNotFoundException
import com.spruhs.kick_app.user.api.UserCreatedEvent
import com.spruhs.kick_app.user.api.UserImageUpdatedEvent
import com.spruhs.kick_app.user.api.UserNickNameChangedEvent
import com.spruhs.kick_app.viewservice.api.UserApi
import com.spruhs.kick_app.viewservice.api.UserData
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
            else -> throw UnknownEventTypeException(event)
        }
    }

    private suspend fun handleUserCreated(event: UserCreatedEvent) {
        repository.save(
            UserProjection(
                id = UserId(event.aggregateId),
                nickName = event.nickName,
                email = event.email,
                groups = emptyList()
            )
        )
    }

    private suspend fun handleNickNameChanged(event: UserNickNameChangedEvent) {
        val user = fetchUser(UserId(event.aggregateId))
        repository.save(user.copy(nickName = event.nickName))
    }

    private suspend fun handleUserImageUpdated(event: UserImageUpdatedEvent) {
        val user = fetchUser(UserId(event.aggregateId))
        repository.save(user.copy(userImageId = event.imageId))
    }

    private suspend fun fetchUser(userId: UserId): UserProjection =
        repository.getUser(userId) ?: throw UserNotFoundException(userId)

    suspend fun getUser(userId: UserId): UserProjection =
        repository.getUser(userId) ?: throw UserNotFoundException(userId)
}

@Service
class UserApiService(
    private val repository: UserProjectionRepository
): UserApi {

    override suspend fun findUsersByIds(userIds: List<UserId>): List<UserData> =
        userIds.map { repository.getUser(it)?.toData() ?: throw UserNotFoundException(it) }

    override suspend fun findUserById(userId: UserId): UserData =
        repository.getUser(userId)?.toData() ?: throw UserNotFoundException(userId)

    override suspend fun existsByEmail(email: String): Boolean = repository.existsByEmail(email)
}

interface UserProjectionRepository {
    suspend fun whenEvent(event: BaseEvent)
    suspend fun existsByEmail(email: String): Boolean
    suspend fun getUser(userId: UserId): UserProjection?
    suspend fun findAll(exceptGroupId: GroupId?): List<UserProjection>
    suspend fun save(userProjection: UserProjection)
}

data class UserProjection (
    val id: UserId,
    val nickName: String,
    val email: String,
    val userImageId: UserImageId? = null,
    val groups: List<GroupProjection> = emptyList(),
)

data class GroupProjection(
    val id: GroupId,
    val name: String,
    val userStatus: PlayerStatusType,
    val userRole: PlayerRole,
    val lastMatch: LocalDateTime? = null,
)

private fun UserProjection.toData() = UserData(
    id = this.id,
    nickName = this.nickName
)