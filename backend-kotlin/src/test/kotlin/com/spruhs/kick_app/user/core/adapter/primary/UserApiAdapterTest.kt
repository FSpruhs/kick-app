package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.user.core.TestUserBuilder
import com.spruhs.kick_app.user.core.application.UserUseCases
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UserApiAdapterTest {

    @MockK
    private lateinit var userUseCases: UserUseCases

    @InjectMockKs
    private lateinit var userApiAdapter: UserApiAdapter

    @Test
    fun `findUsersByIds should return list of UserData`() {
        // given
        val user = TestUserBuilder().build()
        every { userUseCases.getUsersByIds(listOf(user.id)) } returns listOf(user)

        // when
        userApiAdapter.findUsersByIds(listOf(user.id)).let { users ->
            // then
            assertThat(users).hasSize(1)
            assertThat(users.first().id).isEqualTo(user.id)
            assertThat(users.first().nickName).isEqualTo(user.nickName.value)
        }
    }

    @Test
    fun `findUserById should return UserData`() {
        // given
        val user = TestUserBuilder().build()
        every { userUseCases.getUser(user.id) } returns user

        // when
        userApiAdapter.findUserById(user.id).let { userData ->
            // then
            assertThat(userData.id).isEqualTo(user.id)
            assertThat(userData.nickName).isEqualTo(user.nickName.value)
        }
    }
}