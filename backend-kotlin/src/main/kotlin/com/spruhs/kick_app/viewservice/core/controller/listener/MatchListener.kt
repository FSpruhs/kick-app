package com.spruhs.kick_app.viewservice.core.controller.listener

import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.viewservice.core.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component("ViewMatchListener")
class MatchListener(
    private val applicationScope: CoroutineScope,
    private val userService: UserService
) {

    private val log = getLogger(this::class.java)

    @EventListener(MatchResultEnteredEvent::class)
    fun onEvent(event: MatchResultEnteredEvent) {
        log.info("MatchResultEnteredEvent received: $event")
        applicationScope.launch {
            userService.whenEvent(event)
        }
    }
}