package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.es.AggregateStore
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.UserImageId
import com.spruhs.kick_app.user.core.domain.*
import com.spruhs.kick_app.view.api.UserApi
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

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

    suspend fun updateUserImage(userId: UserId, image: FilePart): UserImageId {
        val allowedTypes = setOf("image/jpeg", "image/png", "image/webp", "image/svg")
        val type = "${image.headers().contentType?.type}/${image.headers().contentType?.subtype}"

        require(type in allowedTypes) { "Dateityp nicht erlaubt" }

        val stream = image.content()
            .map { it.asInputStream() }
            .awaitSingle()

        val imageId = userImagePort.save(stream, type)

        aggregateStore.load(userId.value, UserAggregate::class.java).apply {
            updateUserImage(imageId)
            aggregateStore.save(this)
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