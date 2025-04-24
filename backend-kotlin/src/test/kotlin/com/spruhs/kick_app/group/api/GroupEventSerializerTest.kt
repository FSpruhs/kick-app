package com.spruhs.kick_app.group.api

import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.domain.GroupAggregate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class GroupEventSerializerTest {

    @Test
    fun `serialize and deserialize group created event`() {
        // Given
        val groupId = "groupId"
        val aggregate = GroupAggregate(groupId)
        val event = GroupCreatedEvent(groupId, UserId("userId"), "groupName")

        // When
        val serialized = GroupEventSerializer().serialize(event, aggregate)
        val deserialized = GroupEventSerializer().deserialize(serialized)

        // Then
        assertThat(deserialized).isEqualTo(event)
    }

}