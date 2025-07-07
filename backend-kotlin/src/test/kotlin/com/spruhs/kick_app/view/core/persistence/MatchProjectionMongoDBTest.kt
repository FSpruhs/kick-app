package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.AbstractMongoTest
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.match.TestMatchBuilder
import com.spruhs.kick_app.view.core.service.MatchFilter
import com.spruhs.kick_app.view.core.service.MatchProjection
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

class MatchProjectionMongoDBTest : AbstractMongoTest() {

    @Autowired
    private lateinit var adapter: MatchProjectionMongoDB

    @Autowired
    private lateinit var matchRepository: MatchRepository

    @Test
    fun `save should find match`(): Unit = runBlocking {
        // Given
        val match = TestMatchBuilder().toProjection()

        // When
        adapter.save(match)

        // Then
        adapter.findById(match.id).let { result ->
            assertThat(result).isNotNull()
            assertThat(result?.id).isEqualTo(match.id)
            assertThat(result?.groupId).isEqualTo(match.groupId)
            assertThat(result?.start?.toLocalDate()).isEqualTo(match.start.toLocalDate())
            assertThat(result?.playground).isEqualTo(match.playground)
            assertThat(result?.maxPlayer).isEqualTo(match.maxPlayer)
            assertThat(result?.minPlayer).isEqualTo(match.minPlayer)
            assertThat(result?.isCanceled).isEqualTo(match.isCanceled)
            assertThat(result?.cadrePlayers).hasSize(match.cadrePlayers.size)
            assertThat(result?.deregisteredPlayers).hasSize(match.deregisteredPlayers.size)
            assertThat(result?.waitingBenchPlayers).hasSize(match.waitingBenchPlayers.size)
            assertThat(result?.result).hasSize(match.result.size)
            match.result.forEach { player ->
                assertThat(result?.result).anyMatch {
                    it.userId == player.userId && it.playerResult == player.playerResult && it.team == player.team
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    fun `findAllByGroupId should find all with filter`(data: TestData): Unit = runBlocking {
        // Given
        data.matches.forEach { match ->
            adapter.save(match)
        }
        // When
        val result = adapter.findAllByGroupId(
            data.groupId,
            MatchFilter(
                after = data.after,
                before = data.before,
                limit = data.limit
            )
        )

        // Then
        assertThat(result.map { it.id }).containsExactlyElementsOf(data.resultMatches)
    }

    companion object {
        data class TestData(
            val matches: List<MatchProjection>,
            val groupId: GroupId,
            val after: LocalDateTime?,
            val before: LocalDateTime?,
            val limit: Int?,
            val resultMatches: List<MatchId>
        )

        @JvmStatic
        fun data() = listOf(
            TestData(
                matches = listOf(
                    TestMatchBuilder().withId("match1").withStart(LocalDateTime.now().plusDays(2)).withGroupId("group1")
                        .toProjection(),
                    TestMatchBuilder().withId("match2").withStart(LocalDateTime.now().plusDays(3)).withGroupId("group1")
                        .toProjection(),
                    TestMatchBuilder().withId("match3").withStart(LocalDateTime.now().plusDays(4)).withGroupId("group1")
                        .toProjection(),
                    TestMatchBuilder().withId("match4").withStart(LocalDateTime.now().plusDays(1)).withGroupId("group1")
                        .toProjection(),
                    TestMatchBuilder().withId("match5").withStart(LocalDateTime.now().plusDays(5)).withIsCanceled(true)
                        .withGroupId("group1").toProjection(),
                    TestMatchBuilder().withId("match6").withStart(LocalDateTime.now().plusDays(6)).withGroupId("group2").toProjection(),
                ),
                groupId = GroupId("group1"),
                after = LocalDateTime.now(),
                before = null,
                limit = 2,
                resultMatches = listOf(MatchId("match3"), MatchId("match2"))
            ),
            TestData(
                matches = listOf(
                    TestMatchBuilder().withId("match1").withStart(LocalDateTime.now().minusDays(1))
                        .withGroupId("group1").toProjection(),
                    TestMatchBuilder().withId("match2").withStart(LocalDateTime.now().plusDays(1)).withGroupId("group1")
                        .toProjection(),
                ),
                groupId = GroupId("group1"),
                after = null,
                before = LocalDateTime.now(),
                limit = null,
                resultMatches = listOf(MatchId("match1"))
            ),
            TestData(
                matches = listOf(
                    TestMatchBuilder().withId("match1").withStart(LocalDateTime.now().plusDays(1)).withGroupId("group1")
                        .toProjection(),
                    TestMatchBuilder().withId("match2").withStart(LocalDateTime.now().minusDays(1))
                        .withGroupId("group1").toProjection(),
                ),
                groupId = GroupId("group1"),
                after = LocalDateTime.now(),
                before = null,
                limit = null,
                resultMatches = listOf(MatchId("match1"))
            ),
            TestData(
                matches = listOf(
                    TestMatchBuilder().withId("match1").withGroupId("group1").toProjection(),
                    TestMatchBuilder().withId("match2").withGroupId("group2").toProjection(),
                ),
                groupId = GroupId("group1"),
                after = null,
                before = null,
                limit = null,
                resultMatches = listOf(MatchId("match1"))
            )
        )
    }
}