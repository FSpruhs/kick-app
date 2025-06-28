package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.AbstractMongoTest
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class StatisticProjectionMongoAdapterTest : AbstractMongoTest() {

    @Autowired
    private lateinit var adapter: StatisticProjectionMongoAdapter

    @Autowired
    private lateinit var statisticsRepository: StatisticsRepository

    @Test
    fun `onSave should save statistic`(): Unit = runBlocking {
        // Given
        val statistic = TestStatisticBuilder().build()

        // When
        adapter.save(statistic)

        // Then
        adapter.findByPlayer(statistic.groupId, statistic.userId).let {
            assertThat(it).isNotNull()
            assertThat(it?.id).isEqualTo(statistic.id)
            assertThat(it?.groupId).isEqualTo(statistic.groupId)
            assertThat(it?.userId).isEqualTo(statistic.userId)
            assertThat(it?.totalMatches).isEqualTo(statistic.totalMatches)
            assertThat(it?.wins).isEqualTo(statistic.wins)
            assertThat(it?.losses).isEqualTo(statistic.losses)
            assertThat(it?.draws).isEqualTo(statistic.draws)
        }
    }
}