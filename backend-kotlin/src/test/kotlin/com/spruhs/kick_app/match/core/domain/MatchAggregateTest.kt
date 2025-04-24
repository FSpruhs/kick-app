package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.match.core.application.PlanMatchCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime


class MatchAggregateTest {

    @Test
    fun `planMatch should plan a match`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")

        val command = PlanMatchCommand(
            groupId = GroupId("groupId"),
            start = LocalDateTime.now(),
            playground = Playground("stadium"),
            playerCount = PlayerCount(MinPlayer(8), MaxPlayer(16))
        )

        // When
        matchAggregate.planMatch(command)

        // Then
        assertThat(matchAggregate.groupId).isEqualTo(command.groupId)
        assertThat(matchAggregate.start).isEqualTo(command.start)
        assertThat(matchAggregate.playground).isEqualTo(command.playground)
        assertThat(matchAggregate.playerCount).isEqualTo(command.playerCount)
    }

}