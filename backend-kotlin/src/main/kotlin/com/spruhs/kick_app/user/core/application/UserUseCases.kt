package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.es.AggregateStore
import com.spruhs.kick_app.common.helper.KeyedMutex
import com.spruhs.kick_app.common.types.Email
import com.spruhs.kick_app.common.types.ImageType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.UserImageId
import com.spruhs.kick_app.user.api.UserApi
import com.spruhs.kick_app.user.core.domain.NickName
import com.spruhs.kick_app.user.core.domain.UserAggregate
import com.spruhs.kick_app.user.core.domain.UserImagePort
import com.spruhs.kick_app.user.core.domain.UserAlreadyExistsException
import org.springframework.stereotype.Service

@Service
class UserCommandsPort(
    private val aggregateStore: AggregateStore,
    private val userImagePort: UserImagePort,
    private val userApi: UserApi,
    private val mutex: KeyedMutex<UserId> = KeyedMutex(),
) {
    suspend fun registerUser(command: RegisterUserCommand): UserAggregate {
        require(userApi.existsByEmail(command.email).not()) {
            throw UserAlreadyExistsException(command.email, command.userId)
        }

        require(userApi.existsByUserId(command.userId).not()) {
            throw UserAlreadyExistsException(command.email, command.userId)
        }

        return UserAggregate(command.userId.value).also {
            it.createUser(
                email = command.email,
                nickName = command.nickName,
            )
            aggregateStore.save(it)
        }
    }

    suspend fun changeNickName(command: ChangeUserNickNameCommand) =
        handle(command.userId) { user ->
            user.changeNickName(command.nickName)
        }

    suspend fun updateUserImage(
        userId: UserId,
        image: UserImageUpload,
    ): UserImageId {
        val type = image.contentType
        require(type in ImageType.allowedMimeTypes) { "Dateityp nicht erlaubt" }

        val imageId = userImagePort.save(image.bytes.inputStream(), type)

        handle(userId) { user ->
            user.userImageId?.let { userImagePort.delete(it) }
            user.updateUserImage(imageId)
        }

        return imageId
    }

    private suspend fun loadUser(userId: UserId): UserAggregate = aggregateStore.load(userId.value, UserAggregate::class.java)

    private suspend fun handle(
        id: UserId,
        block: suspend (UserAggregate) -> Unit,
    ) {
        mutex.withKeyLock(id) {
            loadUser(id).also {
                block(it)
                aggregateStore.save(it)
            }
        }
    }
}

data class UserImageUpload(
    val bytes: ByteArray,
    val contentType: String,
)

data class RegisterUserCommand(
    val userId: UserId,
    var nickName: NickName,
    var email: Email,
)

data class ChangeUserNickNameCommand(
    val userId: UserId,
    val nickName: NickName,
)
