package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.domain.*
import org.springframework.stereotype.Service

@Service
class UserUseCases(
    private val userPersistencePort: UserPersistencePort,
    private val userIdentityProviderPort: UserIdentityProviderPort
) {
    suspend fun getUser(userId: UserId): User = fetchUser(userId)

    suspend fun userLeavesGroup(userId: UserId, groupId: GroupId) {
        fetchUser(userId).leaveGroup(groupId).apply { userPersistencePort.save(this) }
    }

    suspend fun userEntersGroup(userId: UserId, groupId: GroupId) {
        fetchUser(userId).enterGroup(groupId).apply { userPersistencePort.save(this) }
    }

    suspend fun getUsersByIds(userIds: List<UserId>): List<User> = userPersistencePort.findByIds(userIds)

    suspend fun getUsers(exceptGroupId: GroupId? = null): List<User> = userPersistencePort.findAll(exceptGroupId)

    suspend fun registerUser(command: RegisterUserCommand) {
        require(
            userPersistencePort.existsByEmail(command.email).not()
        ) { UserWithEmailAlreadyExistsException(command.email) }

        createUser(
            FullName(command.firstName, command.lastName),
            command.nickName,
            command.email,
        ).apply {
            userPersistencePort.save(this)
            userIdentityProviderPort.save(this)
        }
    }

    private suspend  fun fetchUser(userId: UserId): User =
        userPersistencePort.findById(userId) ?: throw UserNotFoundException(userId)

}

data class RegisterUserCommand(
    var firstName: FirstName,
    var lastName: LastName,
    var nickName: NickName,
    var email: Email,
)