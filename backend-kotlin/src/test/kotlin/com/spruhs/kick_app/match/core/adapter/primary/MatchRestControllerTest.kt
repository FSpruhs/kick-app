package com.spruhs.kick_app.match.core.adapter.primary

import com.spruhs.kick_app.TestHelpers.jwtWithUserId
import com.spruhs.kick_app.TestSecurityConfig
import com.spruhs.kick_app.common.helper.JWTParser
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.match.TestMatchBuilder
import com.spruhs.kick_app.match.core.application.AddRegistrationCommand
import com.spruhs.kick_app.match.core.application.CancelMatchCommand
import com.spruhs.kick_app.match.core.application.ChangePlaygroundCommand
import com.spruhs.kick_app.match.core.application.MatchCommandPort
import com.spruhs.kick_app.match.core.domain.Playground
import com.spruhs.kick_app.match.core.domain.RegistrationStatusType
import com.spruhs.kick_app.message.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.domain.UserIdentityProviderPort
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
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
@Import(TestSecurityConfig::class, JWTParser::class, MatchRestControllerTest.TestConfig::class)
class MatchRestControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun messageUseCases(): MessageUseCases = mockk(relaxed = true)

        @Bean
        fun matchCommandPort(): MatchCommandPort = mockk(relaxed = true)

        @Bean
        fun userIdentityProviderPort(): UserIdentityProviderPort = mockk(relaxed = true)
    }

    @Autowired
    lateinit var matchCommandPort: MatchCommandPort

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun `planMatch should plan match`() {
        val userId = UserId("testUserId")
        val builder = TestMatchBuilder()
        val match = builder.build()
        val planMatchRequest = builder.toPlanMatchRequest()

        coEvery { matchCommandPort.plan(builder.toPlanMatchCommand(userId)) }.returns(match)

        webTestClient.post()
            .uri("/api/v1/match")
            .header("Authorization", "Bearer ${jwtWithUserId(userId.value)}")
            .bodyValue(planMatchRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody<String>()
            .consumeWith { response ->
                assertNotNull(response.responseBody)
                assertThat(response.responseBody).isEqualTo(match.aggregateId)
            }
    }

    @Test
    fun `cancelMatch should cancel match`() {
        val userId = UserId("testUserId")
        val matchId = MatchId("match123")

        coEvery { matchCommandPort.cancelMatch(CancelMatchCommand(userId, matchId)) }.returns(Unit)

        webTestClient.delete()
            .uri("/api/v1/match/${matchId.value}")
            .header("Authorization", "Bearer ${jwtWithUserId(userId.value)}")
            .exchange()
            .expectStatus().isOk
            .expectBody<Void>()
            .consumeWith { response ->
                assertNull(response.responseBody)
            }
    }

    @Test
    fun `changePlayground should change playground`() {
        val userId = UserId("testUserId")
        val matchId = MatchId("match123")
        val playground = Playground("New Playground")

        coEvery {
            matchCommandPort.changePlayground(
                ChangePlaygroundCommand(
                    userId,
                    matchId,
                    playground
                )
            )
        }.returns(Unit)
        webTestClient.put()
            .uri("/api/v1/match/${matchId.value}/playground?playground=${playground.value}")
            .header("Authorization", "Bearer ${jwtWithUserId(userId.value)}")
            .exchange()
            .expectStatus().isOk
            .expectBody<Void>()
            .consumeWith { response ->
                assertNull(response.responseBody)
            }
    }

    @Test
    fun `updatePlayerRegistration should update player registration`() {
        val userId = UserId("testUserId")
        val matchId = MatchId("match123")
        val updatedUser = UserId("updatedUser")
        val status = RegistrationStatusType.REGISTERED

        coEvery {
            matchCommandPort.addRegistration(
                AddRegistrationCommand(
                    updatedUser,
                    userId,
                    matchId,
                    status
                )
            )
        }.returns(Unit)

        webTestClient.put()
            .uri("/api/v1/match/${matchId.value}/players/${updatedUser.value}?status=${status.name}")
            .header("Authorization", "Bearer ${jwtWithUserId(userId.value)}")
            .exchange()
            .expectStatus().isOk
            .expectBody<Void>()
            .consumeWith { response ->
                assertNull(response.responseBody)
            }
    }

    @Test
    fun `addResult should add result`() {
        val userId = UserId("testUserId")
        val builder = TestMatchBuilder()
        coEvery {
            matchCommandPort.enterResult(builder.toEnterResultCommand(userId))
        }.returns(Unit)

        webTestClient.post()
            .uri("/api/v1/match/${builder.matchId}/result")
            .header("Authorization", "Bearer ${jwtWithUserId(userId.value)}")
            .bodyValue(builder.toEnterResultRequest())
            .exchange()
            .expectStatus().isOk
            .expectBody<Void>()
            .consumeWith { response ->
                assertNull(response.responseBody)
            }
    }

}