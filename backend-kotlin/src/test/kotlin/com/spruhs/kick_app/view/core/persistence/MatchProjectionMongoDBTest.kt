package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.AbstractMongoTest
import com.spruhs.kick_app.match.TestMatchBuilder
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

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
}