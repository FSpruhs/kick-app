package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.TestUserBuilder
import com.spruhs.kick_app.user.core.application.UserQueryPort
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UserApiAdapterTest {

    @MockK
    lateinit var userQueryPort: UserQueryPort

    @InjectMockKs
    lateinit var userApiAdapter: UserApiAdapter

    @Test
    fun `findUserById should find user by id`(): Unit = runBlocking {
        // Given
        val userBuilder = TestUserBuilder()
        coEvery { userQueryPort.getUser(UserId(userBuilder.id)) } returns userBuilder.buildProjection()

        // When
        val result = userApiAdapter.findUserById(UserId(userBuilder.id))

        // Then
        assertThat(result).isEqualTo(userBuilder.buildData())
    }

    @Test
    fun `findUsersByIds should find users by ids`(): Unit = runBlocking {
        // Given
        val userBuilder = TestUserBuilder()
        val userIdList = listOf(UserId(userBuilder.id))
        coEvery { userQueryPort.getUsersByIds(userIdList) } returns listOf(userBuilder.buildProjection())

        // When
        val result = userApiAdapter.findUsersByIds(userIdList)

        // Then
        assertThat(result).isEqualTo(listOf(userBuilder.buildData()))
    }

}