package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.application.GroupUseCases
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GroupApiAdapterTest {

    @MockK
    lateinit var groupUseCases: GroupUseCases

    @InjectMockKs
    lateinit var adapter: GroupApiAdapter

    @Test
    fun `isActiveMember should return true when user is active member`(): Unit = runBlocking {
        val groupId = GroupId("group id")
        val userId = UserId("user id")

        coEvery { groupUseCases.isActiveMember(groupId, userId) } returns true

        adapter.isActiveMember(groupId, userId).let {
            assertThat(it).isTrue()
        }
    }

    @Test
    fun `isActiveAdmin should return true when user is active admin`(): Unit = runBlocking {
        val groupId = GroupId("group id")
        val userId = UserId("user id")

        coEvery { groupUseCases.isActiveAdmin(groupId, userId) } returns true

        adapter.isActiveAdmin(groupId, userId).let {
            assertThat(it).isTrue()
        }
    }

    @Test
    fun `getActivePlayers should return list of active players`(): Unit = runBlocking {
        val groupId = GroupId("group id")
        val userIds = listOf(UserId("user id"))

        coEvery { groupUseCases.getActivePlayers(groupId) } returns userIds

        adapter.getActivePlayers(groupId).let {
            assertThat(it).containsExactlyElementsOf(userIds)
        }
    }

}