package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.user.api.UserCreatedEvent
import com.spruhs.kick_app.user.api.UserNickNameChangedEvent
import com.spruhs.kick_app.user.core.domain.UserProjectionPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class UserListener(
    private val applicationScope: CoroutineScope,
    private val userProjectionPort: UserProjectionPort,
) {
    private val log = getLogger(this::class.java)

    @EventListener(UserCreatedEvent::class)
    fun onEvent(event: UserCreatedEvent) {
        log.info("UserCreatedEvent received: $event")
        applicationScope.launch {
            userProjectionPort.whenEvent(event)
        }
    }

    @EventListener(UserNickNameChangedEvent::class)
    fun onEvent(event: UserNickNameChangedEvent) {
        log.info("UserNickNameChangedEvent received: $event")
        applicationScope.launch {
            userProjectionPort.whenEvent(event)
        }
    }
}