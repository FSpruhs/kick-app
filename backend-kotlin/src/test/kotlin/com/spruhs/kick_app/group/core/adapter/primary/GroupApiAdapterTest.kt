package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.application.GroupQueryPort
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class GroupApiAdapterTest {

    @MockK
    lateinit var groupQueryPort: GroupQueryPort

    @InjectMockKs
    lateinit var groupApiAdapter: GroupApiAdapter

    @Test
    fun `isActiveMember should return true when active member`(): Unit = runBlocking {
        // Given
        val groupId = GroupId("groupId")
        val userId = UserId("userId")
        coEvery { groupQueryPort.isActiveMember(groupId, userId) } returns true

        // When
        val result = groupApiAdapter.isActiveMember(groupId, userId)


        // Then
        assertThat(result).isTrue
    }

    @Test
    fun `isActiveAdmin should return true when active admin`(): Unit = runBlocking {
        // Given
        val groupId = GroupId("groupId")
        val userId = UserId("userId")
        coEvery { groupQueryPort.isActiveCoach(groupId, userId) } returns true

        // When
        val result = groupApiAdapter.isActiveCoach(groupId, userId)


        // Then
        assertThat(result).isTrue
    }

    @Test
    fun `getActivePlayers should return list of active players`(): Unit = runBlocking {
        // Given
        val groupId = GroupId("groupId")
        val userId1 = UserId("userId1")
        val userId2 = UserId("userId2")
        coEvery { groupQueryPort.getActivePlayers(groupId) } returns listOf(userId1, userId2)

        // When
        val result = groupApiAdapter.getActivePlayers(groupId)

        // Then
        assertThat(result).containsExactlyInAnyOrder(userId1, userId2)
    }

}