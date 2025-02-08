package com.spruhs.kick_app.group.core.application

import com.spruhs.kick_app.common.EventPublisher
import com.spruhs.kick_app.group.TestGroupBuilder
import com.spruhs.kick_app.group.core.domain.GroupPersistencePort
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GroupUseCasesTest {

    @MockK
    lateinit var groupPersistencePort: GroupPersistencePort

    @MockK
    lateinit var eventPublisher: EventPublisher

    @InjectMockKs
    lateinit var useCases: GroupUseCases

    @Test
    fun `create should save group to persistence`() {
        val command = TestGroupBuilder().buildCreateGroupCommand()

        every { groupPersistencePort.save(any()) } just Runs

        useCases.create(command)

        verify { groupPersistencePort.save(any()) }
    }

    @Test
    fun `inviteUser should save group to persistence and publish events`() {
        val command = TestGroupBuilder().buildInviteUserCommand()
        val group = TestGroupBuilder().withInvitedUsers(listOf()).build()

        every { groupPersistencePort.findById(command.groupId) } returns group
        every { groupPersistencePort.save(any()) } just Runs
        every { eventPublisher.publishAll(any()) } just Runs

        useCases.inviteUser(command)

        verify { groupPersistencePort.save(any()) }
        verify { eventPublisher.publishAll(any()) }
    }
}