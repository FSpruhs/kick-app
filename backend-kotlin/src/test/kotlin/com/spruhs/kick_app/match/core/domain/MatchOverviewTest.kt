package com.spruhs.kick_app.match.core.domain

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.match.api.MatchNumberChangedEvent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class MatchOverviewTest {
    @Test
    fun `add should add match`() {
        // Given
        val overview =
            MatchOverview(
                groupId = GroupId("groupId"),
                entries =
                    mutableListOf(
                        MatchOverviewEntry(
                            matchId = MatchId("match1"),
                            matchNumber = 1,
                            start = LocalDateTime.now().plusDays(1),
                            state = MatchState.PLANNED,
                        ),
                        MatchOverviewEntry(
                            matchId = MatchId("match2"),
                            matchNumber = 2,
                            start = LocalDateTime.now().plusDays(3),
                            state = MatchState.PLANNED,
                        ),
                    ),
            )

        // When
        val result = overview.add(MatchId("match3"), LocalDateTime.now().plusDays(2))

        // Then
        assertThat(result).isEqualTo(2)
        assertThat(overview.entries).hasSize(3)
        assertThat(overview.events).hasSize(1)
        assertThat(overview.events.first()).isEqualTo(MatchNumberChangedEvent("match2", 3))
    }

    @Test
    fun `add should throw exception when match already exists`() {
        // Given
        val overview =
            MatchOverview(
                groupId = GroupId("groupId"),
                entries =
                    mutableListOf(
                        MatchOverviewEntry(
                            matchId = MatchId("match1"),
                            matchNumber = 1,
                            start = LocalDateTime.now().plusDays(1),
                            state = MatchState.PLANNED,
                        ),
                    ),
            )

        // When + Then
        assertThatThrownBy { overview.add(MatchId("match1"), LocalDateTime.now().plusDays(2)) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `add should throw exception when start is in the past`() {
        // Given
        val overview =
            MatchOverview(
                groupId = GroupId("groupId"),
            )

        // When + Then
        assertThatThrownBy { overview.add(MatchId("match1"), LocalDateTime.now().minusDays(1)) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `add should return 1 when overview is empty`() {
        // Given
        val overview =
            MatchOverview(
                groupId = GroupId("groupId"),
            )

        // When
        val result = overview.add(MatchId("match1"), LocalDateTime.now().plusDays(1))

        // Then
        assertThat(result).isEqualTo(1)
    }

    @Test
    fun `add should return 2 when second match`() {
        // Given
        val overview =
            MatchOverview(
                groupId = GroupId("groupId"),
                entries =
                    mutableListOf(
                        MatchOverviewEntry(
                            matchId = MatchId("match1"),
                            matchNumber = 1,
                            start = LocalDateTime.now().plusDays(1),
                            state = MatchState.PLANNED,
                        ),
                    ),
            )

        // When
        val result = overview.add(MatchId("match2"), LocalDateTime.now().plusDays(2))

        // Then
        assertThat(result).isEqualTo(2)
        assertThat(overview.entries).hasSize(2)
        assertThat(overview.events).isEmpty()
    }
}
