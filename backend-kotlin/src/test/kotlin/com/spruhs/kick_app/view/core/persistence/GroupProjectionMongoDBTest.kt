package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.AbstractMongoTest
import com.spruhs.kick_app.group.TestGroupBuilder
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GroupProjectionMongoDBTest : AbstractMongoTest() {

    @Autowired
    private lateinit var adapter: GroupProjectionMongoDB

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @Test
    fun `save should save group`(): Unit = runBlocking {
        // Given
        val group = TestGroupBuilder().buildProjection()

        // When
        adapter.save(group)

        // Then
        adapter.findById(group.id).let { result ->
            assertThat(result).isNotNull()
            assertThat(result?.id).isEqualTo(group.id)
            assertThat(result?.name).isEqualTo(group.name)
            assertThat(result?.players).hasSize(group.players.size)
        }
    }
}