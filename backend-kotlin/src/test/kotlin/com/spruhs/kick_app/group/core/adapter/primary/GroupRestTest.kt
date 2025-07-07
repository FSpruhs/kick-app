package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.TestHelpers.jwtWithUserId
import com.spruhs.kick_app.TestSecurityConfig
import com.spruhs.kick_app.common.helper.JWTParser
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.group.TestGroupBuilder
import com.spruhs.kick_app.group.core.application.GroupCommandPort
import com.spruhs.kick_app.group.core.domain.Active
import com.spruhs.kick_app.group.core.domain.Player
import com.spruhs.kick_app.user.core.domain.UserIdentityProviderPort
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestSecurityConfig::class, JWTParser::class, GroupRestTest.TestConfig::class)
class GroupRestTest {
    @TestConfiguration
    class TestConfig {
        @Bean
        fun groupCommandPort(): GroupCommandPort = mockk(relaxed = true)

        @Bean
        fun userIdentityProviderPort(): UserIdentityProviderPort = mockk(relaxed = true)
    }

    @Autowired
    lateinit var groupCommandPort: GroupCommandPort

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `updatePlayer should update player status and role`() {
        val requestingUser = UserId("testUserId")
        val builder = TestGroupBuilder().withPlayers(listOf(Player(
            id = UserId("testPlayerId"),
            status = Active(),
            role = PlayerRole.PLAYER
        )))
        val group = builder.build()
        val newStatus = PlayerStatusType.ACTIVE
        val newRole = PlayerRole.COACH

        coEvery { groupCommandPort.updatePlayerStatus(builder.toUpdatePlayerStatusCommand(requestingUser, newStatus)) } returns Unit
        coEvery { groupCommandPort.updatePlayerRole(builder.toUpdatePlayerRoleCommand(requestingUser, newRole)) } returns Unit

        webTestClient.put()
            .uri("/api/v1/group/${group.aggregateId}/players/${group.players.first().id.value}?status=${newStatus.name}&role=${newRole.name}")
            .header("Authorization", "Bearer ${jwtWithUserId(requestingUser.value)}")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `createGroup should create group`() {
        val requestingUser = UserId("testUserId")
        val builder = TestGroupBuilder()
        val group = builder.build()

        coEvery { groupCommandPort.createGroup(builder.toCreateGroupCommand(requestingUser)) } returns group

        webTestClient.post()
            .uri("/api/v1/group")
            .header("Authorization", "Bearer ${jwtWithUserId(requestingUser.value)}")
            .bodyValue(builder.toCreateGroupRequest())
            .exchange()
            .expectStatus().isCreated
            .expectBody<String>()
            .consumeWith { response ->
                assertNotNull(response.responseBody)
                assertEquals(group.aggregateId, response.responseBody)
            }
    }

    @Test
    fun `updateGroupName should update group name`() {
        val requestingUser = UserId("testUserId")
        val builder = TestGroupBuilder()
        val group = builder.build()
        val newName = "New Group Name"

        coEvery { groupCommandPort.changeGroupName(builder.toUpdateGroupNameCommand(requestingUser, newName)) } returns Unit

        webTestClient.put()
            .uri("/api/v1/group/${group.aggregateId}/name?name=$newName")
            .header("Authorization", "Bearer ${jwtWithUserId(requestingUser.value)}")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `inviteUser should invite user`() {
        val requestingUser = UserId("testUserId")
        val builder = TestGroupBuilder()
        val group = builder.build()
        val userIdToInvite = UserId("userToInvite")

        coEvery { groupCommandPort.inviteUser(builder.toInviteUserCommand(requestingUser, userIdToInvite)) } returns Unit

        webTestClient.post()
            .uri("/api/v1/group/${group.aggregateId}/invited-users/${userIdToInvite.value}")
            .header("Authorization", "Bearer ${jwtWithUserId(requestingUser.value)}")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `invitedUserResponse should handle user response`() {
        val invitedUser = UserId("userToInvite")
        val builder = TestGroupBuilder().withInvitedPlayers(listOf(invitedUser.value))
        val response = true

        coEvery { groupCommandPort.inviteUserResponse(builder.toInvitedUserResponseCommand(invitedUser, response)) } returns Unit

        webTestClient.put()
            .uri("/api/v1/group/invited-users")
            .header("Authorization", "Bearer ${jwtWithUserId(invitedUser.value)}")
            .bodyValue(builder.toInvitedUserResponse(response))
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `invitedUserResponse should return bad request if requesting user not invited user`() {
        val invitedUser = UserId("userToInvite")
        val builder = TestGroupBuilder().withInvitedPlayers(listOf(invitedUser.value))

        webTestClient.put()
            .uri("/api/v1/group/invited-users")
            .header("Authorization", "Bearer ${jwtWithUserId("anotherUser")}")
            .bodyValue(builder.toInvitedUserResponse(true))
            .exchange()
            .expectStatus().isUnauthorized
    }
}