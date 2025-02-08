package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.match.api.MatchCreatedEvent
import com.spruhs.kick_app.user.core.application.MessageParams
import com.spruhs.kick_app.user.core.application.MessageUseCases
import com.spruhs.kick_app.user.core.domain.MessageType
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class MatchListenerTest {

    @MockK
    lateinit var messageUseCases: MessageUseCases

    @InjectMockKs
    lateinit var listener: MatchListener

    @Test
    fun `onEvent should send message when MatchCreatedEvent is received`() {
        // given
        val event = MatchCreatedEvent("matchId", "start", LocalDateTime.now())
        every { messageUseCases.sendAllActiveUsersInGroupMessage(any(), any(),GroupId(event.groupId)) } returns Unit

        // when
        listener.onEvent(event)

        // then
        verify {
            messageUseCases.sendAllActiveUsersInGroupMessage(
                MessageType.MATCH_CREATED,
                MessageParams(matchId = event.matchId, start = event.start, groupId = event.groupId),
                GroupId(event.groupId)
            )
        }
    }
}