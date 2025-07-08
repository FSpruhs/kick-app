package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.exceptions.UserNotFoundException
import com.spruhs.kick_app.user.TestUserBuilder
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class UserApiServiceTest {
    @MockK
    lateinit var repository: UserProjectionRepository

    @InjectMockKs
    lateinit var service: UserApiService

    @Test
    fun `findUserById should return users by id`(): Unit = runBlocking {
        // Given
        val user = TestUserBuilder().buildProjection()

        coEvery { repository.getUser(user.id) } returns user

        // When
        service.findUserById(user.id).let { result ->
            // Then
            assertThat(result.id).isEqualTo(user.id)
            assertThat(result.nickName).isEqualTo(user.nickName)
            assertThat(result.email).isEqualTo(user.email)
        }
    }

    @Test
    fun `findUsersById should throw exception when user not found`(): Unit = runBlocking {
        // Given
        val userId = UserId("testUserId")

        coEvery { repository.getUser(userId) } returns null

        // When
        assertFailsWith<UserNotFoundException> { service.findUserById(userId) }
    }

    @Test
    fun `existsByEmail should return true when user exists with given email`(): Unit = runBlocking {
        // Given
        val email = "test@testen.com"

        coEvery { repository.existsByEmail(email) } returns true

        // When
        service.existsByEmail(email).let { result ->

            // Then
            assertThat(result).isTrue
        }
    }

    @Test
    fun `getGroups should get groups`(): Unit = runBlocking {
        // Given
        val user = TestUserBuilder().buildProjection()
        coEvery { repository.getUser(user.id) } returns user

        // When
        service.getGroups(user.id).let { result ->
            // Then
            assertThat(result).isNotEmpty
            assertThat(result).containsExactlyInAnyOrderElementsOf(user.groups.map { it.id })
        }
    }
}