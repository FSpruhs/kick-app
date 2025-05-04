package com.spruhs.kick_app.common

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

interface EventPublisher {
    fun publish(events: List<Any>)
}

@Service
class EventPublisherAdapter(val applicationEventPublisher: ApplicationEventPublisher) : EventPublisher {

    private val log = getLogger(this::class.java)

    override fun publish(events: List<Any>) {
        events.forEach {
            log.info("Publish event: ${it.javaClass.simpleName}")
            applicationEventPublisher.publishEvent(it)
        }
    }
}

