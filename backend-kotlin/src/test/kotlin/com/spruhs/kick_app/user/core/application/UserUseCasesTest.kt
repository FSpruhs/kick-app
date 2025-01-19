package com.spruhs.kick_app.user.core.application

import com.spruhs.kick_app.user.core.TestUserBuilder
import com.spruhs.kick_app.user.core.domain.UserIdentityProviderPort
import com.spruhs.kick_app.user.core.domain.UserPersistencePort
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserUseCasesTest {

    @MockK
    lateinit var userPersistencePort: UserPersistencePort

    @MockK
    lateinit var userIdentityProviderPort: UserIdentityProviderPort

    @InjectMockKs
    lateinit var useCases: UserUseCases

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

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
}


