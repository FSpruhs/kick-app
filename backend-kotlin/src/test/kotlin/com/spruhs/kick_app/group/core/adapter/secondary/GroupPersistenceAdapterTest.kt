package com.spruhs.kick_app.group.core.adapter.secondary

import com.spruhs.kick_app.AbstractMongoTest
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.TestGroupBuilder
import com.spruhs.kick_app.group.core.domain.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GroupPersistenceAdapterTest : AbstractMongoTest() {

    @Autowired
    private lateinit var groupPersistencePort: GroupPersistencePort

    @Test
    fun `save should save group to repository`(): Unit = runBlocking {
        val group = TestGroupBuilder().build()

        groupPersistencePort.save(group)

        groupPersistencePort.findById(group.id).let { result ->
            assertThat(result).isEqualTo(group)
        }
    }

    @Test
    fun `find by id should return null when group not exists`() = runBlocking {
        val group = TestGroupBuilder().build()

        groupPersistencePort.save(group)

        groupPersistencePort.findById(GroupId("not-exists")).let { result ->
            assertThat(result).isNull()
        }
    }

    @Test
    fun `find by player should return groups when player exists`(): Unit = runBlocking {
        val playerToFind = Player(UserId("test player"), Active(), PlayerRole.ADMIN)

        val group1 = TestGroupBuilder()
            .withId("test id 1")
            .withPlayers(listOf(playerToFind))
            .build()

        val group2 = TestGroupBuilder()
            .withId("test id 2")
            .withPlayers(listOf(playerToFind))
            .build()

        val group3 = TestGroupBuilder()
            .withId("test id 3")
            .withPlayers(listOf(Player(UserId("player not to find"), Active(), PlayerRole.ADMIN)))
            .build()

        groupPersistencePort.save(group1)
        groupPersistencePort.save(group2)
        groupPersistencePort.save(group3)

        groupPersistencePort.findByPlayer(playerToFind.id).let { result ->
            assertThat(result.map { it.id.value }).containsExactlyInAnyOrder("test id 1", "test id 2")
        }
    }
}