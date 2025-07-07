package com.spruhs.kick_app.match.api

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.user.core.domain.UserAggregate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime

class MatchEventSerializerTest {

    @ParameterizedTest
    @MethodSource("data")
    fun `serialize and deserialize match events`(event: Any) {
        // Given
        val matchId = "matchId"
        val aggregate = UserAggregate(matchId)
        val event = event

        // When
        val serialized = MatchEventSerializer().serialize(event, aggregate)
        val deserialized = MatchEventSerializer().deserialize(serialized)

        // Then
        assertThat(deserialized).isEqualTo(event)
    }

    companion object {
        @JvmStatic
        fun data() = listOf(
            MatchPlannedEvent("matchId", GroupId("groupId"), LocalDateTime.now(), "playground", 10, 5),
            PlayerAddedToCadreEvent("matchId", UserId("userId"), "status"),
            PlayerDeregisteredEvent("matchId", UserId("userId"), "status"),
            PlayerPlacedOnWaitingBenchEvent("matchId", UserId("userId"), "status"),
            MatchCanceledEvent("matchId", GroupId("groupId")),
            PlaygroundChangedEvent("matchId", "newPlayground", GroupId("groupId")),
            MatchResultEnteredEvent("matchId", GroupId("groupId"), listOf(ParticipatingPlayer(UserId("user"), PlayerResult.WIN, MatchTeam.A)), LocalDateTime.now()),
        )
    }
}