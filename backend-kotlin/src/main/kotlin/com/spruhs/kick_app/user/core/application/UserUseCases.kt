package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.AggregateStore
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserImageId
import com.spruhs.kick_app.user.core.domain.*
import com.spruhs.kick_app.view.api.UserApi
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class UserCommandsPort(
    private val aggregateStore: AggregateStore,
    private val userIdentityProviderPort: UserIdentityProviderPort,
    private val userImagePort: UserImagePort,
    private val userApi: UserApi,
) {
    suspend fun registerUser(command: RegisterUserCommand): UserAggregate {
        require(userApi.existsByEmail(command.email.value).not()) {
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

data class RegisterUserCommand(
    var nickName: NickName,
    var email: Email,
    var password: Password? = null
)

data class ChangeUserNickNameCommand(
    val userId: UserId,
    val nickName: NickName
)