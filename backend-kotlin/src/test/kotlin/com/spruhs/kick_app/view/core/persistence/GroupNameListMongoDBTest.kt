package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.AbstractMongoTest
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.view.core.service.GroupNameListEntry
import com.spruhs.kick_app.view.core.service.GroupNameListProjection
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class GroupNameListMongoDBTest  : AbstractMongoTest() {

    @Autowired
    private lateinit var adapter: GroupNameListMongoDB

    @Autowired
    private lateinit var groupNameListRepository: GroupNameListRepository

    @Test
    fun `save should save group name list`(): Unit = runBlocking {
        // Given
        val groupId = GroupId("groupId")
        val groupNameList = GroupNameListProjection(
            groupId = groupId,
            players = listOf(
                GroupNameListEntry(
                    userId = UserId("player1"),
                    name = "Max",
                    imageUrl = "https://example.com/image1.jpg"
                ),
                GroupNameListEntry(
                    userId = UserId("player2"),
                    name = "Melanie",
                    imageUrl = "https://example.com/image2.jpg"
                )
            )
        )

        // When
        adapter.save(groupNameList)

        // Then
        adapter.findByGroupId(groupId).let { result ->
            assertThat(result).isNotNull()
            assertThat(result?.groupId).isEqualTo(groupId)
            assertThat(result?.players).hasSize(groupNameList.players.size)
            assertThat(result?.players?.first()).isEqualTo(groupNameList.players.first())
            assertThat(result?.players?.get(1)).isEqualTo(groupNameList.players.get(1))
        }

    }

    @Test
    fun `findByUserId should find by user id`(): Unit = runBlocking {
        // Given
        val player = UserId("player1")
        val nameList1 = GroupNameListProjection(
            groupId = GroupId("group1"),
            players = listOf(
                GroupNameListEntry(
                    userId = player,
                    name = "Max",
                ),
                GroupNameListEntry(
                    userId = UserId("player2"),
                    name = "Melanie",
                )
            )
        )

        val nameList2 = GroupNameListProjection(
            groupId = GroupId("group2"),
            players = listOf(
                GroupNameListEntry(
                    userId = UserId("player3"),
                    name = "Max",
                ),
                GroupNameListEntry(
                    userId = UserId("player2"),
                    name = "Melanie",
                )
            )
        )

        val nameList3 = GroupNameListProjection(
            groupId = GroupId("group3"),
            players = listOf(
                GroupNameListEntry(
                    userId = player,
                    name = "Max",
                ),
            )
        )

        // When
        adapter.save(nameList1)
        adapter.save(nameList2)
        adapter.save(nameList3)

        // Then
        adapter.findByUserId(player).let { result ->
            assertThat(result).hasSize(2)
            assertThat(result.map { it.groupId }).containsExactlyInAnyOrder(GroupId("group1"), GroupId("group3"))
        }
    }

}