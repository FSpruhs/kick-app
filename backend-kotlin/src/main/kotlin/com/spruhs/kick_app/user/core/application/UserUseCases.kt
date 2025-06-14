package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.AggregateStore
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserImageId
import com.spruhs.kick_app.user.core.domain.*
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class UserCommandsPort(
    private val aggregateStore: AggregateStore,
    private val userIdentityProviderPort: UserIdentityProviderPort,
    private val userQueryPort: UserQueryPort,
    private val userImagePort: UserImagePort,
) {
    suspend fun registerUser(command: RegisterUserCommand): UserAggregate {
        require(userQueryPort.existsByEmail(command.email).not()) {
            throw UserWithEmailAlreadyExistsException(command.email)
        }

        val newId = userIdentityProviderPort.save(command.email, command.nickName, command.password)

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

    suspend fun updateUserImage(userId: UserId, image: MultipartFile): UserImageId {
        val allowedTypes = listOf("image/jpeg", "image/png", "image/webp", "image/gif")
        if (image.contentType !in allowedTypes) {
            throw IllegalArgumentException("Dateityp nicht erlaubt")
        }
        val imageId = userImagePort.save(image.inputStream, image.contentType ?: "image/jpeg")

        aggregateStore.load(userId.value, UserAggregate::class.java).let {
            it.updateUserImage(imageId)
            aggregateStore.save(it)
        }
        return imageId
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
    var password: Password? = null
)

data class ChangeUserNickNameCommand(
    val userId: UserId,
    val nickName: NickName
)