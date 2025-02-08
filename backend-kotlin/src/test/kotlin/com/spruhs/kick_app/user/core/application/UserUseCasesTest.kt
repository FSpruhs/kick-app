package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.user.core.TestUserBuilder
import com.spruhs.kick_app.user.core.domain.UserIdentityProviderPort
import com.spruhs.kick_app.user.core.domain.UserNotFoundException
import com.spruhs.kick_app.user.core.domain.UserPersistencePort
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class UserUseCasesTest {

    @MockK
    lateinit var userPersistencePort: UserPersistencePort

    @MockK
    lateinit var userIdentityProviderPort: UserIdentityProviderPort

    @InjectMockKs
    lateinit var useCases: UserUseCases

    @Test
    fun `registerUser should save user to persistence and identity provider`() {
        val command = TestUserBuilder().buildRegisterUserCommand()
        every { userPersistencePort.existsByEmail(command.email) } returns false

        every { userPersistencePort.save(any()) } just Runs
        every { userIdentityProviderPort.save(any()) } just Runs

        useCases.registerUser(command)

        verify { userPersistencePort.save(any()) }
        verify { userIdentityProviderPort.save(any()) }
    }

    @Test
    fun `registerUser should throw exception if email already exists`() {
        val command = TestUserBuilder().buildRegisterUserCommand()
        every { userPersistencePort.existsByEmail(command.email) } returns true

        assertThatThrownBy { useCases.registerUser(command) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `getUser should return user from persistence`() {
        val user = TestUserBuilder().build()
        every { userPersistencePort.findById(user.id) } returns user

        val result = useCases.getUser(user.id)

        assertThat(result).isEqualTo(user)
    }

    @Test
    fun `getUser should throw exception if user not found`() {
        val user = TestUserBuilder().build()
        every { userPersistencePort.findById(user.id) } returns null

        assertThatThrownBy { useCases.getUser(user.id) }
            .isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `getUsers should return all users from persistence`() {
        val users = listOf(TestUserBuilder().withId("test id 1").build(), TestUserBuilder().withId("test id 2").build())
        every { userPersistencePort.findAll() } returns users

        val result = useCases.getUsers()

        assertThat(result).isEqualTo(users)
    }

    @Test
    fun `getUsersByIds should return users by ids from persistence`() {
        val users = listOf(
            TestUserBuilder().withId("test id 1").build(),
            TestUserBuilder().withId("test id 2").build()
        )
        val userIds = users.map { it.id }
        every { userPersistencePort.findByIds(userIds) } returns users

        val result = useCases.getUsersByIds(userIds)

        assertThat(result).isEqualTo(users)
    }

    @Test
    fun `userLeavesGroup should remove user from group`() {
        val user = TestUserBuilder().build()
        val groupId = GroupId("test group id")
        every { userPersistencePort.findById(user.id) } returns user
        every { userPersistencePort.save(any()) } just Runs

        useCases.userLeavesGroup(user.id, groupId)

        verify { userPersistencePort.save(any()) }
    }

    @Test
    fun `userEntersGroup should add user to group`() {
        val user = TestUserBuilder().build()
        val groupId = GroupId("test group id")
        every { userPersistencePort.findById(user.id) } returns user
        every { userPersistencePort.save(any()) } just Runs

        useCases.userEntersGroup(user.id, groupId)

        verify { userPersistencePort.save(any()) }
    }
}


