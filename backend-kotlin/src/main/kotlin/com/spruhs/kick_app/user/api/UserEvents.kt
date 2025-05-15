package com.spruhs.kick_app.user.api

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.user.core.domain.UserAggregate
import org.springframework.stereotype.Component

data class UserCreatedEvent(
    override val aggregateId: String,
    val email: String,
    val nickName: String,
) : BaseEvent(aggregateId)

data class UserNickNameChangedEvent(
    override val aggregateId: String,
    val nickName: String
) : BaseEvent(aggregateId)

data class UserImageUpdatedEvent(
    override val aggregateId: String,
    val imageId: UserImageId
) : BaseEvent(aggregateId)

enum class UserEvents {
    USER_CREATED_V1,
    USER_NICKNAME_CHANGED_V1,
}

@Component
class UserEventSerializer : Serializer {
    override fun serialize(event: Any, aggregate: AggregateRoot): Event {
        val data = EventSourcingUtils.writeValueAsBytes(event)

        return when (event) {
            is UserCreatedEvent -> Event(
                aggregate,
                UserEvents.USER_CREATED_V1.name,
                data,
                event.metadata
            )

            is UserNickNameChangedEvent -> Event(
                aggregate,
                UserEvents.USER_NICKNAME_CHANGED_V1.name,
                data,
                event.metadata
            )

            else -> throw UnknownEventTypeException(event)
        }
    }

    override fun deserialize(event: Event): Any {
        return when (event.type) {
            UserEvents.USER_CREATED_V1.name -> EventSourcingUtils.readValue(
                event.data, UserCreatedEvent::class.java
            )

            UserEvents.USER_NICKNAME_CHANGED_V1.name -> EventSourcingUtils.readValue(
                event.data, UserNickNameChangedEvent::class.java
            )

            else -> throw UnknownEventTypeException(event)
        }
    }

    override fun aggregateTypeName(): String {
        return UserAggregate::class.simpleName ?: "UserAggregate"
    }
}