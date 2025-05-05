package com.spruhs.kick_app.group.api

import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.domain.GroupAggregate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource


class GroupEventSerializerTest {

    @ParameterizedTest
    @MethodSource("data")
    fun `serialize and deserialize group created event`(event: Any) {
        // Given
        val groupId = "groupId"
        val aggregate = GroupAggregate(groupId)
        val event = event

        // When
        val serialized = GroupEventSerializer().serialize(event, aggregate)
        val deserialized = GroupEventSerializer().deserialize(serialized)

        // Then
        assertThat(deserialized).isEqualTo(event)
    }

    companion object
    {
        @JvmStatic
        fun data() = listOf(
            GroupCreatedEvent("groupId", UserId("userId"), "groupName"),
            GroupNameChangedEvent("groupId", "newGroupName"),
            PlayerInvitedEvent("groupId", UserId("userId"), "groupName"),
            PlayerEnteredGroupEvent("groupId", UserId("userId"), "groupName"),
            PlayerRejectedGroupEvent("groupId", UserId("userId")),
            PlayerPromotedEvent("groupId", UserId("userId")),
            PlayerDowngradedEvent("groupId", UserId("userId")),
            PlayerActivatedEvent("groupId", UserId("userId")),
            PlayerDeactivatedEvent("groupId", UserId("userId")),
            PlayerRemovedEvent("groupId", UserId("userId"), "groupName"),
            PlayerLeavedEvent("groupId", UserId("userId")),
        )
    }
}