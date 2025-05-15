package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.AggregateStore
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.domain.Email
import com.spruhs.kick_app.user.core.domain.NickName
import com.spruhs.kick_app.user.core.domain.UserAggregate
import com.spruhs.kick_app.user.core.domain.UserIdentityProviderPort
import com.spruhs.kick_app.user.core.domain.UserImagePort
import com.spruhs.kick_app.user.core.domain.UserWithEmailAlreadyExistsException
import io.mockk.coEvery
import io.mockk.coVerify
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
    lateinit var userQueryPort: UserQueryPort

    @MockK
    lateinit var userImagePort: UserImagePort

    @InjectMockKs
    lateinit var userCommandsPort: UserCommandsPort

    @Test
    fun `registerUser should throw exception if email exists`(): Unit = runBlocking {
        // Given
        val command = RegisterUserCommand(NickName("Test"), Email("test@testen.com"))
        coEvery { userQueryPort.existsByEmail(command.email) } returns true

        // When + Then
        assertFailsWith<UserWithEmailAlreadyExistsException> {
            userCommandsPort.registerUser(command)
        }
    }


    @Test
    fun `changeNickName should change user nick name`(): Unit = runBlocking {
        // Given
        val command = ChangeUserNickNameCommand(UserId("1234"), NickName("NewNick"))
        coEvery {
            aggregateStore.load(
                command.userId.value,
                UserAggregate::class.java
            )
        } returns UserAggregate(command.userId.value)
        coEvery { userIdentityProviderPort.changeNickName(command.userId, command.nickName) } returns Unit
        coEvery { aggregateStore.save(any()) } returns Unit

        // When
        userCommandsPort.changeNickName(command)

        // Then
        coVerify { userIdentityProviderPort.changeNickName(command.userId, command.nickName) }
    }

}