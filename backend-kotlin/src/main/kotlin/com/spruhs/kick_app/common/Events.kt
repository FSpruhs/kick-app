package com.spruhs.kick_app.common

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

interface DomainEventList {
    val domainEvents: List<DomainEvent>
}

fun interface EventPublisher {
    fun publishAll(events: List<DomainEvent>)
}

fun interface DomainEvent {
    fun eventVersion(): Int
}

@Service
class EventPublisherAdapter(val applicationEventPublisher: ApplicationEventPublisher) : EventPublisher {

    private val log = getLogger(this::class.java)

    override fun publishAll(events: List<DomainEvent>) {
        events.forEach {
            applicationEventPublisher.publishEvent(it)
            log.info("Event published: $it")
        }
    }
}

