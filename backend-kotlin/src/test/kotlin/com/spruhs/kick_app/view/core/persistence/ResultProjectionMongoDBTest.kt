package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.AbstractMongoTest
import com.spruhs.kick_app.common.MatchId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.match.api.MatchTeam
import com.spruhs.kick_app.match.api.PlayerResult
import com.spruhs.kick_app.view.core.service.PlayerResultProjection
import com.spruhs.kick_app.view.core.service.ResultProjection
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ResultProjectionMongoDBTest : AbstractMongoTest() {

    @Autowired
    private lateinit var adapter: ResultProjectionMongoDB

    @Autowired
    private lateinit var resultRepository: ResultRepository


    @Test
    fun `save should save result`(): Unit = runBlocking  {
        // Given
        val matchId = MatchId("test-match-id")
        val testId = "test-id"
        val user1 = UserId("user-id-1")
        val user2 = UserId("user-id-2")
        val result = ResultProjection(
            id = testId,
            matchId = matchId,
            players = mapOf(
                user1 to PlayerResultProjection(
                    matchResult = PlayerResult.WIN,
                    team = MatchTeam.A
                ),
                user2 to PlayerResultProjection(
                    matchResult = PlayerResult.LOSS,
                    team = MatchTeam.B
                ),
            )
        )

        // When
        adapter.save(result)

        // Then
        adapter.findByMatchId(matchId).let { result ->
            assertThat(result).isNotNull()
            assertThat(result?.id).isEqualTo(testId)
            assertThat(result?.matchId).isEqualTo(matchId)
            assertThat(result?.players).hasSize(2)
            assertThat(result?.players[user1]).isEqualTo(PlayerResultProjection(
                matchResult = PlayerResult.WIN,
                team = MatchTeam.A
            ))
            assertThat(result?.players[user2]).isEqualTo(PlayerResultProjection(
                matchResult = PlayerResult.LOSS,
                team = MatchTeam.B
            ))
        }
    }
}