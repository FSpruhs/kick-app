package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.AggregateStore
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.domain.*
import org.springframework.stereotype.Service

@Service
class UserCommandsPort(
    private val aggregateStore: AggregateStore,
    private val userIdentityProviderPort: UserIdentityProviderPort,
    private val userQueryPort: UserQueryPort
) {
    suspend fun registerUser(command: RegisterUserCommand): UserAggregate {
        require(userQueryPort.existsByEmail(command.email).not()) {
            throw UserWithEmailAlreadyExistsException(command.email)
        }

        val newId = userIdentityProviderPort.save(command.email, command.nickName)

        return UserAggregate(newId.value).also {
            it.createUser(command)
            aggregateStore.save(it)
        }
    }

    suspend fun changeNickName(command: ChangeUserNickNameCommand) {
        aggregateStore.load(command.userId.value, UserAggregate::class.java).let {
            userIdentityProviderPort.changeNickName(command.userId, command.nickName)
            it.changeNickName(command)
            aggregateStore.save(it)
        }
    }
}


@Service
class UserQueryPort(
    private val projectionPort: UserProjectionPort
) {

    suspend fun existsByEmail(email: Email): Boolean = projectionPort.existsByEmail(email)

    suspend fun getUser(userId: UserId): UserProjection =
        projectionPort.getUser(userId) ?: throw UserNotFoundException(userId)

    suspend fun getUsersByIds(userIds: List<UserId>): List<UserProjection> =
        userIds.map { projectionPort.getUser(it) ?: throw UserNotFoundException(it) }

    suspend fun getUsers(exceptGroupId: GroupId? = null): List<UserProjection> = projectionPort.findAll(exceptGroupId)
}

data class RegisterUserCommand(
    var nickName: NickName,
    var email: Email,
)

data class ChangeUserNickNameCommand(
    val userId: UserId,
    val nickName: NickName
)