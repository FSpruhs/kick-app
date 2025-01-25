package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.user.core.domain.Message
import com.spruhs.kick_app.user.core.domain.MessagePersistencePort
import com.spruhs.kick_app.user.core.domain.MessageVariables
import com.spruhs.kick_app.user.core.domain.UserInvitedToGroupMessage
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MessageUseCases(val messagePersistencePort: MessagePersistencePort) {

    fun <T: Message> send(messageKClass: KClass<T>, params: Map<String, String>): Message {
        return createMessage(messageKClass, params).apply {
            messagePersistencePort.save(this)
        }
    }

}

private fun getParams(params: Map<String, String>, name: String) = params[name] ?: throw IllegalArgumentException("$name is required")

private fun <T : Message> createMessage(
    messageKClass: KClass<T>,
    params: Map<String, String>
) = when (messageKClass) {
    UserInvitedToGroupMessage::class -> {
        UserInvitedToGroupMessage(
            userId = getParams(params, MessageVariables.USER_ID),
            groupId = getParams(params, MessageVariables.GROUP_ID),
            groupName = getParams(params, MessageVariables.GROUP_NAME)
        )
    }

    else -> {
        throw IllegalArgumentException("Message type not supported")
    }
}
