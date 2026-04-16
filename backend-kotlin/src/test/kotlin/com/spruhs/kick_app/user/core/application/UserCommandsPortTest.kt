package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.es.AggregateStore
import com.spruhs.kick_app.common.types.Email
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.user.api.UserApi
import com.spruhs.kick_app.user.core.domain.NickName
import com.spruhs.kick_app.user.core.domain.UserIdentityProviderPort
import com.spruhs.kick_app.user.core.domain.UserImagePort
import com.spruhs.kick_app.user.core.domain.UserWithEmailAlreadyExistsException
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class UserCommandsPortTest {
    @MockK
    lateinit var aggregateStore: AggregateStore

    @MockK
    lateinit var userIdentityProviderPort: UserIdentityProviderPort

    @MockK
    lateinit var userImagePort: UserImagePort

    @MockK
    lateinit var userApi: UserApi

    @InjectMockKs
    lateinit var userCommandsPort: UserCommandsPort

    @Test
    fun `registerUser should throw exception if email exists`(): Unit =
        runBlocking {
            // Given
            val command = RegisterUserCommand(UserId("testUserId"), NickName("Test"), Email("test@testen.com"))
            coEvery { userApi.existsByEmail(command.email) } returns true

            // When + Then
            assertFailsWith<UserWithEmailAlreadyExistsException> {
                userCommandsPort.registerUser(command)
            }
        }
}
