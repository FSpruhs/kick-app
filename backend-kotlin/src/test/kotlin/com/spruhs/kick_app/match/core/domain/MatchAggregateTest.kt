package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.match.core.application.PlanMatchCommand
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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

    @Test
    fun `changePlayground should change playground`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.playground = Playground("stadium")

        // When
        matchAggregate.changePlayground(Playground("arena"))

        // Then
        assertThat(matchAggregate.playground).isEqualTo(Playground("arena"))
    }

    @Test
    fun `startMatch should start match`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now()

        // When
        matchAggregate.startMatch()

        // Then
        assertThat(matchAggregate.status).isEqualTo(MatchStatus.ENTER_RESULT)
    }

    @Test
    fun `startMatch should throw exception if start is in the future`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().plusDays(1)


        assertThatThrownBy {
            // When
            matchAggregate.startMatch()

            // Then
        }.isInstanceOf(MatchStartTimeException::class.java)
    }

    @Test
    fun `cancelMatch should cancel match`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now().plusDays(1)

        // When
        matchAggregate.cancelMatch()

        // Then
        assertThat(matchAggregate.status).isEqualTo(MatchStatus.CANCELLED)
    }

    @Test
    fun `cancelMatch should throw exception if start is in the past`() {
        // Given
        val matchAggregate = MatchAggregate("matchId")
        matchAggregate.start = LocalDateTime.now()


        assertThatThrownBy {
            // When
            matchAggregate.cancelMatch()

            // Then
        }.isInstanceOf(MatchStartTimeException::class.java)
    }

}