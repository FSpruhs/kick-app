package com.spruhs.kick_app.group.api

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.es.UnknownEventTypeException
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.group.core.domain.GroupAggregate
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchEventSerializer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.Test


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

    companion object {
        @JvmStatic
        fun data() = listOf(
            GroupCreatedEvent("groupId", UserId("userId"), "groupName", PlayerStatusType.ACTIVE, PlayerRole.COACH),
            GroupNameChangedEvent("groupId", "newGroupName"),
            PlayerInvitedEvent("groupId", UserId("userId"), "groupName"),
            PlayerEnteredGroupEvent("groupId", UserId("userId"), "groupName", PlayerStatusType.ACTIVE, PlayerRole.PLAYER),
            PlayerRejectedGroupEvent("groupId", UserId("userId")),
            PlayerPromotedEvent("groupId", UserId("userId")),
            PlayerDowngradedEvent("groupId", UserId("userId")),
            PlayerActivatedEvent("groupId", UserId("userId")),
            PlayerDeactivatedEvent("groupId", UserId("userId")),
            PlayerRemovedEvent("groupId", UserId("userId"), "groupName"),
            PlayerLeavedEvent("groupId", UserId("userId")),
        )
    }

    @Test
    fun `serialized should throw exception when event unknown`() {
        // Given
        val groupId = "groupId"
        val aggregate = GroupAggregate(groupId)
        val event = MatchCanceledEvent("matchId", GroupId("groupId"))

        // When

        assertThatThrownBy {
            GroupEventSerializer().serialize(event, aggregate)

        // Then
        }.isInstanceOf(UnknownEventTypeException::class.java)
    }

    @Test
    fun `deserialize should throw exception when event unknown`() {
        // Given
        val groupId = "groupId"
        val aggregate = GroupAggregate(groupId)
        val event = MatchCanceledEvent("matchId", GroupId("groupId"))
        val serialized = MatchEventSerializer().serialize(event, aggregate)

        assertThatThrownBy {
            GroupEventSerializer().deserialize(serialized)

        // Then
        }.isInstanceOf(UnknownEventTypeException::class.java)
    }
}