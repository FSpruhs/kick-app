package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.es.AggregateStore
import com.spruhs.kick_app.common.types.Email
import com.spruhs.kick_app.common.types.ImageType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.UserImageId
import com.spruhs.kick_app.user.api.UserApi
import com.spruhs.kick_app.user.core.domain.NickName
import com.spruhs.kick_app.user.core.domain.UserAggregate
import com.spruhs.kick_app.user.core.domain.UserIdentityProviderPort
import com.spruhs.kick_app.user.core.domain.UserImagePort
import com.spruhs.kick_app.user.core.domain.UserWithEmailAlreadyExistsException
import org.springframework.stereotype.Service

@Service
class UserCommandsPort(
    private val aggregateStore: AggregateStore,
    private val userImagePort: UserImagePort,
    private val userApi: UserApi,
) {
    suspend fun registerUser(command: RegisterUserCommand): UserAggregate {
        require(userApi.existsByEmail(command.email).not()) {
            throw UserWithEmailAlreadyExistsException(command.email)
        }

        return UserAggregate(command.userId.value).also {
            it.createUser(
                email = command.email,
                nickName = command.nickName,
            )
            aggregateStore.save(it)
        }
    }

    suspend fun changeNickName(command: ChangeUserNickNameCommand) {
        aggregateStore.load(command.userId.value, UserAggregate::class.java).let {
            it.changeNickName(
                nickName = command.nickName,
            )
            aggregateStore.save(it)
        }
    }

    suspend fun updateUserImage(
        userId: UserId,
        image: UserImageUpload,
    ): UserImageId {
        val type = image.contentType
        require(type in ImageType.allowedMimeTypes) { "Dateityp nicht erlaubt" }

        val stream = image.bytes.inputStream()

        val imageId = userImagePort.save(stream, type)

        aggregateStore.load(userId.value, UserAggregate::class.java).apply {
            this.userImageId?.let { userImagePort.delete(it) }
            updateUserImage(imageId)
            aggregateStore.save(this)
        }

        return imageId
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
