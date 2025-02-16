package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.application.GroupUseCases
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
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
    fun `isActiveMember should return true when user is active member`() {
        val groupId = GroupId("group id")
        val userId = UserId("user id")

        every { groupUseCases.isActiveMember(groupId, userId) } returns true

        adapter.isActiveMember(groupId, userId).let {
            assertThat(it).isTrue()
        }
    }

    @Test
    fun `isActiveAdmin should return true when user is active admin`() {
        val groupId = GroupId("group id")
        val userId = UserId("user id")

        every { groupUseCases.isActiveAdmin(groupId, userId) } returns true

        adapter.isActiveAdmin(groupId, userId).let {
            assertThat(it).isTrue()
        }
    }

    @Test
    fun `getActivePlayers should return list of active players`() {
        val groupId = GroupId("group id")
        val userIds = listOf(UserId("user id"))

        every { groupUseCases.getActivePlayers(groupId) } returns userIds

        adapter.getActivePlayers(groupId).let {
            assertThat(it).containsExactlyElementsOf(userIds)
        }
    }

}