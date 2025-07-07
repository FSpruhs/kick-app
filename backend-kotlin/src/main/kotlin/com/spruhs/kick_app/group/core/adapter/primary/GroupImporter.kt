package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.es.AggregateStore
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.SampleDataImporter
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.helper.getLogger
import com.spruhs.kick_app.group.api.GroupCreatedEvent
import com.spruhs.kick_app.group.api.PlayerDeactivatedEvent
import com.spruhs.kick_app.group.api.PlayerEnteredGroupEvent
import com.spruhs.kick_app.group.api.PlayerLeavedEvent
import com.spruhs.kick_app.group.api.PlayerPromotedEvent
import com.spruhs.kick_app.group.api.PlayerRemovedEvent
import com.spruhs.kick_app.group.core.domain.GroupAggregate
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Profile("dev")
@Order(2)
class GroupImporter(private val aggregateStore: AggregateStore) : SampleDataImporter {

    private val log = getLogger(this::class.java)

    override suspend fun import() {
        log.info("Starting to load sample group data...")
        importFirstGroup()
        importSecondGroup()
        log.info("Sample group data loaded")
    }

    private suspend fun importSecondGroup() {
        val groupId = GroupId("group-id-2")
        val groupAggregate = GroupAggregate(groupId.value)
        groupAggregate.apply(
            GroupCreatedEvent(
                aggregateId = groupId.value,
                name = "Jahnwiesen",
                userId = UserId("user-id-1"),
                userStatus = PlayerStatusType.ACTIVE,
                userRole = PlayerRole.COACH,
            )
        )
        defaultUserIds.subList(0, defaultUserIds.size / 2).forEach {
            groupAggregate.apply(PlayerEnteredGroupEvent(
                aggregateId = groupId.value,
                userId = it,
                groupName = "Montags kick",
                userStatus = PlayerStatusType.ACTIVE,
                userRole = PlayerRole.PLAYER,
            ))
        }

        groupAggregate.apply(PlayerDeactivatedEvent(
            aggregateId = groupId.value,
            userId = UserId("user-id-2"),
        ))
        groupAggregate.apply(PlayerLeavedEvent(
            aggregateId = groupId.value,
            userId = UserId("user-id-3"),
        ))
        groupAggregate.apply(PlayerRemovedEvent(
            aggregateId = groupId.value,
            userId = UserId("user-id-4"),
            groupName = groupAggregate.name.value,
        ))
        groupAggregate.apply(PlayerPromotedEvent(
            aggregateId = groupId.value,
            userId = UserId("user-id-5"),
        ))
        aggregateStore.save(groupAggregate)
    }

    private suspend fun importFirstGroup() {
        val groupId = GroupId("group-id-1")
        val groupAggregate = GroupAggregate(groupId.value)
        groupAggregate.apply(
            GroupCreatedEvent(
                aggregateId = groupId.value,
                name = "Donnerstags kick",
                userId = UserId("user-id-1"),
                userStatus = PlayerStatusType.ACTIVE,
                userRole = PlayerRole.COACH,
            )
        )
        defaultUserIds.forEach {
            groupAggregate.apply(PlayerEnteredGroupEvent(
                aggregateId = groupId.value,
                userId = it,
                groupName = groupAggregate.name.value,
                userStatus = PlayerStatusType.ACTIVE,
                userRole = PlayerRole.PLAYER,
            ))
        }
        groupAggregate.apply(PlayerDeactivatedEvent(
            aggregateId = groupId.value,
            userId = UserId("user-id-2"),
        ))
        groupAggregate.apply(PlayerLeavedEvent(
            aggregateId = groupId.value,
            userId = UserId("user-id-19"),
        ))
        groupAggregate.apply(PlayerRemovedEvent(
            aggregateId = groupId.value,
            userId = UserId("user-id-20"),
            groupName = groupAggregate.name.value,
        ))
        groupAggregate.apply(PlayerPromotedEvent(
            aggregateId = groupId.value,
            userId = UserId("user-id-3"),
        ))
        groupAggregate.apply(PlayerPromotedEvent(
            aggregateId = groupId.value,
            userId = UserId("user-id-13"),
        ))
        aggregateStore.save(groupAggregate)
    }
}

private val defaultUserIds = listOf(
    UserId("user-id-2"),
    UserId("user-id-3"),
    UserId("user-id-4"),
    UserId("user-id-5"),
    UserId("user-id-6"),
    UserId("user-id-7"),
    UserId("user-id-8"),
    UserId("user-id-9"),
    UserId("user-id-10"),
    UserId("user-id-11"),
    UserId("user-id-12"),
    UserId("user-id-13"),
    UserId("user-id-14"),
    UserId("user-id-15"),
    UserId("user-id-16"),
    UserId("user-id-17"),
    UserId("user-id-18"),
    UserId("user-id-19"),
    UserId("user-id-20"),
)