package com.spruhs.kick_app.user.core.domain

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.user.core.TestUserBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserTest {
    @Test
    fun `should create a user`() {
        val firstName = FirstName("John")
        val lastName = LastName("Doe")
        val fullName = FullName(firstName, lastName)
        val nickName = NickName("john_doe")
        val email = Email("john@doe.com")
        val user = createUser(
            fullName = fullName,
            nickName = nickName,
            email = email,
        )

        assertThat(user.fullName).isEqualTo(fullName)
        assertThat(user.nickName).isEqualTo(nickName)
        assertThat(user.email).isEqualTo(email)
        assertThat(user.groups).isEmpty()
        assertThat(user.id).isNotNull()
        assertThat(user.id.value).isNotEmpty()
    }

    @Test
    fun `should leave group`() {
        val groupId = GroupId("group id")
        val user = TestUserBuilder().withGroups(listOf(groupId.value)).build()

        user.leaveGroup(groupId).let { result ->
            assertThat(result.groups).isEmpty()
        }
    }

    @Test
    fun `should enter group`() {
        val groupId = GroupId("group id")
        val user = TestUserBuilder().withGroups(emptyList()).build()

        user.enterGroup(groupId).let { result ->
            assertThat(result.groups).containsExactlyInAnyOrder(groupId)
        }
    }
}