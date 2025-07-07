package com.spruhs.kick_app.match.core.application

import com.spruhs.kick_app.common.es.AggregateStore
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.UserNotAuthorizedException
import com.spruhs.kick_app.match.TestMatchBuilder
import com.spruhs.kick_app.match.core.domain.MatchAggregate
import com.spruhs.kick_app.match.core.domain.Playground
import com.spruhs.kick_app.match.core.domain.RegistrationStatusType
import com.spruhs.kick_app.view.api.GroupApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class MatchCommandPortTest {

    @MockK
    lateinit var aggregateStore: AggregateStore

    @MockK
    lateinit var groupApi: GroupApi

    @InjectMockKs
    lateinit var matchCommandPort: MatchCommandPort

    @Test
    fun `plan should plan match`(): Unit = runBlocking {
        val requestingUserId = UserId("requestingUser")
        val builder = TestMatchBuilder()
        val command = builder.toPlanMatchCommand(requestingUserId)

        coEvery { groupApi.isActiveMember(command.groupId, requestingUserId) } returns true
        coEvery { aggregateStore.save(any()) } returns Unit

        matchCommandPort.plan(command).let { result ->
            assertThat(result.aggregateId).isNotEmpty()
        }
    }

    @Test
    fun `plan should throw UserNotAuthorizedException when user is not an active member`(): Unit = runBlocking {
        val requestingUserId = UserId("requestingUser")
        val builder = TestMatchBuilder()
        val command = builder.toPlanMatchCommand(requestingUserId)

        coEvery { groupApi.isActiveMember(command.groupId, requestingUserId) } returns false

        assertFailsWith<UserNotAuthorizedException> {
            matchCommandPort.plan(command)
        }
    }

    @Test
    fun `cancelMatch should cancel match`(): Unit = runBlocking {
        val requestingUser = UserId("user")
        val builder = TestMatchBuilder().withStart(LocalDateTime.now().plusDays(1))
        val match = builder.build()
        val command = builder.toCancelMatchCommand(requestingUser)

        coEvery { aggregateStore.load(command.matchId.value, MatchAggregate::class.java) } returns match
        coEvery { groupApi.isActiveCoach(match.groupId, requestingUser) } returns true
        coEvery { aggregateStore.save(any()) } returns Unit

        matchCommandPort.cancelMatch(command)

        coVerify { aggregateStore.save(any()) }
    }

    @Test
    fun `cancelMatch should throw UserNotAuthorizedException when user is not an active coach`(): Unit = runBlocking {
        val requestingUser = UserId("user")
        val builder = TestMatchBuilder().withStart(LocalDateTime.now().plusDays(1))
        val match = builder.build()
        val command = builder.toCancelMatchCommand(requestingUser)

        coEvery { aggregateStore.load(command.matchId.value, MatchAggregate::class.java) } returns match
        coEvery { groupApi.isActiveCoach(match.groupId, requestingUser) } returns false

        assertFailsWith<UserNotAuthorizedException> {
            matchCommandPort.cancelMatch(command)
        }
    }

    @Test
    fun `changePlayground should change playground of match`(): Unit = runBlocking {
        val requestingUser = UserId("user")
        val builder = TestMatchBuilder().withStart(LocalDateTime.now().plusDays(1))
        val match = builder.build()
        val command = builder.toChangePlaygroundCommand(requestingUser, Playground("New Playground"))

        coEvery { aggregateStore.load(command.matchId.value, MatchAggregate::class.java) } returns match
        coEvery { groupApi.isActiveCoach(match.groupId, requestingUser) } returns true
        coEvery { aggregateStore.save(any()) } returns Unit

        matchCommandPort.changePlayground(command)

        coVerify { aggregateStore.save(any()) }
    }

    @Test
    fun `changePlayground should throw UserNotAuthorizedException when user is not an active coach`(): Unit = runBlocking {
        val requestingUser = UserId("user")
        val builder = TestMatchBuilder()
        val match = builder.build()
        val command = builder.toChangePlaygroundCommand(requestingUser, Playground("New Playground"))

        coEvery { aggregateStore.load(command.matchId.value, MatchAggregate::class.java) } returns match
        coEvery { groupApi.isActiveCoach(match.groupId, requestingUser) } returns false

        assertFailsWith<UserNotAuthorizedException> {
            matchCommandPort.changePlayground(command)
        }
    }

    @ParameterizedTest
    @MethodSource("selfRegistrationStatus")
    fun `addRegistration should add registered to match`(registrationStatus: RegistrationStatusType): Unit = runBlocking {
        val updatingUser = UserId("updatingUser")
        val updatedUser = UserId("updatingUser")
        val builder = TestMatchBuilder()
        val match = builder.build()

        val command = builder.toAddRegistrationCommand(updatingUser, updatedUser, registrationStatus)

        coEvery { aggregateStore.load(command.matchId.value, MatchAggregate::class.java) } returns match
        coEvery { groupApi.isActiveMember(match.groupId, updatedUser) } returns true
        coEvery { aggregateStore.save(any()) } returns Unit

        matchCommandPort.addRegistration(command)

        coVerify { aggregateStore.save(any()) }
    }

    @ParameterizedTest
    @MethodSource("selfRegistrationStatus")
    fun `addRegistration should throw UserNotAuthorizedException for invalid registration status`(registrationStatus: RegistrationStatusType): Unit = runBlocking {
        val updatingUser = UserId("updatingUser")
        val updatedUser = UserId("updatedUser")
        val builder = TestMatchBuilder()
        val match = builder.build()
        val command = builder.toAddRegistrationCommand(updatingUser, updatedUser, registrationStatus)
        coEvery { aggregateStore.load(command.matchId.value, MatchAggregate::class.java) } returns match
        coEvery { groupApi.isActiveMember(match.groupId, updatedUser) } returns true

        assertFailsWith<UserNotAuthorizedException> {
            matchCommandPort.addRegistration(command)
        }
    }

    @ParameterizedTest
    @MethodSource("otherRegistrationStatus")
    fun `addRegistration should add registration to match`(registrationStatus: RegistrationStatusType): Unit = runBlocking {
        val updatingUser = UserId("updatingUser")
        val updatedUser = UserId("updatedUser")
        val builder = TestMatchBuilder()
        val match = builder.build()
        val command = builder.toAddRegistrationCommand(updatingUser, updatedUser, registrationStatus)

        coEvery { aggregateStore.load(command.matchId.value, MatchAggregate::class.java) } returns match
        coEvery { groupApi.isActiveMember(match.groupId, updatedUser) } returns true
        coEvery { groupApi.isActiveCoach(match.groupId, updatingUser) } returns true
        coEvery { aggregateStore.save(any()) } returns Unit

        matchCommandPort.addRegistration(command)

        coVerify { aggregateStore.save(any()) }
    }

    @ParameterizedTest
    @MethodSource("otherRegistrationStatus")
    fun `addRegistration should throw UserNotAuthorizedException for invalid registration status type`(registrationStatus: RegistrationStatusType): Unit = runBlocking {
        val updatingUser = UserId("updatingUser")
        val updatedUser = UserId("updatedUser")
        val builder = TestMatchBuilder()
        val match = builder.build()
        val command = builder.toAddRegistrationCommand(updatingUser, updatedUser, registrationStatus)
        coEvery { aggregateStore.load(command.matchId.value, MatchAggregate::class.java) } returns match
        coEvery { groupApi.isActiveMember(match.groupId, updatedUser) } returns true
        coEvery { groupApi.isActiveCoach(match.groupId, updatingUser) } returns false

        assertFailsWith<UserNotAuthorizedException> {
            matchCommandPort.addRegistration(command)
        }
    }

    @Test
    fun `enterResult should enter result for match`(): Unit = runBlocking {
        val userId = UserId("user")
        val builder = TestMatchBuilder()
        val match = builder.build()
        val command = builder.toEnterResultCommand(userId)

        coEvery { aggregateStore.load(command.matchId.value, MatchAggregate::class.java) } returns match
        coEvery { groupApi.isActiveCoach(match.groupId, userId) } returns true
        coEvery { aggregateStore.save(any()) } returns Unit

        matchCommandPort.enterResult(command)

        coVerify { aggregateStore.save(any()) }
    }

    @Test
    fun `enterResult should throw UserNotAuthorizedException when user is not an active coach`(): Unit = runBlocking {
        val userId = UserId("user")
        val builder = TestMatchBuilder()
        val match = builder.build()
        val command = builder.toEnterResultCommand(userId)
        coEvery { aggregateStore.load(command.matchId.value, MatchAggregate::class.java) } returns match
        coEvery { groupApi.isActiveCoach(match.groupId, userId) } returns false
        assertFailsWith<UserNotAuthorizedException> {
            matchCommandPort.enterResult(command)
        }
    }

    companion object {
        @JvmStatic
        fun selfRegistrationStatus() = listOf(
            RegistrationStatusType.REGISTERED,
            RegistrationStatusType.DEREGISTERED
        )

        @JvmStatic
        fun otherRegistrationStatus() = listOf(
            RegistrationStatusType.CANCELLED,
            RegistrationStatusType.ADDED,
        )
    }

}