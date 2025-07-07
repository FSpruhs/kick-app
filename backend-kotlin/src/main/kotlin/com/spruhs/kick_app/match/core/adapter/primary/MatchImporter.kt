package com.spruhs.kick_app.match.core.adapter.primary

import com.spruhs.kick_app.common.es.AggregateStore
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.SampleDataImporter
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.helper.getLogger
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchPlannedEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.api.MatchTeam
import com.spruhs.kick_app.match.api.ParticipatingPlayer
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlayerResult
import com.spruhs.kick_app.match.api.PlaygroundChangedEvent
import com.spruhs.kick_app.match.core.domain.MatchAggregate
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@Profile("dev")
@Order(3)
class MatchImporter(
    private val aggregateStore: AggregateStore,
) : SampleDataImporter {

    private val log = getLogger(this::class.java)

    override suspend fun import() {
        log.info("Starting to load sample match data...")
        val groupId1 = GroupId("group-id-1")
        val groupId2 = GroupId("group-id-2")

        importOldMatches(groupId1)
        createCanceledMatch(
            MatchId("match-id-11"),
            groupId1,
            LocalDateTime.now().minusDays(1),
        ).also { aggregateStore.save(it) }
        createCanceledMatch(
            MatchId("match-id-12"),
            groupId1,
            LocalDateTime.now().plusDays(1),
        ).also { aggregateStore.save(it) }

        createUpcomingMatch(
            matchId = MatchId("match-id-13"),
            groupId = groupId1,
            start = LocalDateTime.now().plusDays(7),
        ).also { aggregateStore.save(it) }
        createUpcomingMatch(
            matchId = MatchId("match-id-14"),
            groupId = groupId1,
            start = LocalDateTime.now().plusDays(14),
        ).also { aggregateStore.save(it) }

        createEmptyMatch(
            matchId = MatchId("match-id-16"),
            groupId = groupId2,
            start = LocalDateTime.now().plusDays(11),
        ).also { aggregateStore.save(it) }

        log.info("Sample match data loaded!")
    }

    private suspend fun createEmptyMatch(
        matchId: MatchId,
        groupId: GroupId,
        start: LocalDateTime,
    ): MatchAggregate {
        val match = MatchAggregate(matchId.value)
        match.apply(MatchPlannedEvent(
            aggregateId = matchId.value,
            groupId = groupId,
            start = start,
            maxPlayer = 14,
            minPlayer = 8
        ))
        return match
    }

    private fun createCanceledMatch(
        matchId: MatchId,
        groupId: GroupId,
        start: LocalDateTime
    ): MatchAggregate {
        val match = MatchAggregate(matchId.value)
        match.apply(MatchPlannedEvent(
            aggregateId = matchId.value,
            groupId = groupId,
            start = start,
            maxPlayer = 14,
            minPlayer = 8
        ))
        match.apply(MatchCanceledEvent(
            aggregateId = matchId.value,
            groupId = groupId,
        ))
        return match
    }

    private suspend fun importOldMatches(groupId: GroupId) {
        oldMatches.forEach { (matchId, daysAgo, isDraw) ->
            createOldMatch(
                matchId = MatchId(matchId),
                groupId = groupId,
                start = LocalDateTime.now().minusDays(daysAgo),
                isDraw = isDraw
            ).also { aggregateStore.save(it) }
        }
    }

    private suspend fun createUpcomingMatch(
        matchId: MatchId,
        groupId: GroupId,
        start: LocalDateTime,
    ): MatchAggregate {
        val shuffledUsers = activeGroup1UserIds.shuffled().take(16)
        val match = MatchAggregate(matchId.value)
        match.apply(MatchPlannedEvent(
            aggregateId = matchId.value,
            groupId = groupId,
            start = start,
            maxPlayer = 12,
            minPlayer = 8
        ))
        match.apply(PlayerAddedToCadreEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[0],
            status = "REGISTERED"
        ))
        match.apply(PlayerAddedToCadreEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[1],
            status = "REGISTERED"
        ))
        match.apply(PlayerAddedToCadreEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[2],
            status = "REGISTERED"
        ))
        match.apply(PlayerAddedToCadreEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[3],
            status = "REGISTERED"
        ))
        match.apply(PlayerAddedToCadreEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[4],
            status = "REGISTERED"
        ))
        match.apply(PlayerAddedToCadreEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[5],
            status = "REGISTERED"
        ))
        match.apply(PlayerAddedToCadreEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[6],
            status = "REGISTERED"
        ))
        match.apply(PlayerAddedToCadreEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[7],
            status = "REGISTERED"
        ))
        match.apply(PlayerAddedToCadreEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[8],
            status = "REGISTERED"
        ))
        match.apply(PlayerAddedToCadreEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[9],
            status = "REGISTERED"
        ))
        match.apply(PlayerAddedToCadreEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[10],
            status = "REGISTERED"
        ))
        match.apply(PlayerAddedToCadreEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[11],
            status = "REGISTERED"
        ))
        match.apply(PlayerPlacedOnWaitingBenchEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[12],
            status = "REGISTERED"
        ))
        match.apply(PlayerPlacedOnWaitingBenchEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[13],
            status = "REGISTERED"
        ))
        match.apply(PlayerDeregisteredEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[14],
            status = "DEREGISTERED"
        ))
        match.apply(PlayerDeregisteredEvent(
            aggregateId = matchId.value,
            userId = shuffledUsers[15],
            status = "DEREGISTERED"
        ))
        return match
    }


    private suspend fun createOldMatch(
        matchId: MatchId,
        groupId: GroupId,
        start: LocalDateTime,
        isDraw: Boolean = false
        ): MatchAggregate {
        val shuffledUsers = allGroup1UserIds.shuffled().take(12)
        val match = MatchAggregate(matchId.value)
        match.apply(MatchPlannedEvent(
            aggregateId = matchId.value,
            groupId = groupId,
            start = start,
            maxPlayer = 14,
            minPlayer = 8
        ))
        match.apply(PlaygroundChangedEvent(
            aggregateId = matchId.value,
            newPlayground = "Olympiahalle",
            groupId = groupId,
        ))
        match.apply(MatchResultEnteredEvent(
            aggregateId = matchId.value,
            groupId = groupId,
            players = listOf(
                ParticipatingPlayer(
                    userId = shuffledUsers[0],
                    playerResult = if (isDraw) PlayerResult.DRAW else PlayerResult.WIN,
                    team = MatchTeam.A
                ),
                ParticipatingPlayer(
                    userId = shuffledUsers[1],
                    playerResult = if (isDraw) PlayerResult.DRAW else PlayerResult.WIN,
                    team = MatchTeam.A
                ),
                ParticipatingPlayer(
                    userId = shuffledUsers[2],
                    playerResult = if (isDraw) PlayerResult.DRAW else PlayerResult.WIN,
                    team = MatchTeam.A
                ),
                ParticipatingPlayer(
                    userId = shuffledUsers[3],
                    playerResult = if (isDraw) PlayerResult.DRAW else PlayerResult.WIN,
                    team = MatchTeam.A
                ),
                ParticipatingPlayer(
                    userId = shuffledUsers[4],
                    playerResult = if (isDraw) PlayerResult.DRAW else PlayerResult.WIN,
                    team = MatchTeam.A
                ),
                ParticipatingPlayer(
                    userId = shuffledUsers[5],
                    playerResult = if (isDraw) PlayerResult.DRAW else PlayerResult.WIN,
                    team = MatchTeam.A
                ),
                ParticipatingPlayer(
                    userId = shuffledUsers[6],
                    playerResult = if (isDraw) PlayerResult.DRAW else PlayerResult.LOSS,
                    team = MatchTeam.B
                ),
                ParticipatingPlayer(
                    userId = shuffledUsers[7],
                    playerResult = if (isDraw) PlayerResult.DRAW else PlayerResult.LOSS,
                    team = MatchTeam.B
                ),
                ParticipatingPlayer(
                    userId = shuffledUsers[8],
                    playerResult = if (isDraw) PlayerResult.DRAW else PlayerResult.LOSS,
                    team = MatchTeam.B
                ),
                ParticipatingPlayer(
                    userId = shuffledUsers[9],
                    playerResult = if (isDraw) PlayerResult.DRAW else PlayerResult.LOSS,
                    team = MatchTeam.B
                ),
                ParticipatingPlayer(
                    userId = shuffledUsers[10],
                    playerResult = if (isDraw) PlayerResult.DRAW else PlayerResult.LOSS,
                    team = MatchTeam.B
                ),
                ParticipatingPlayer(
                    userId = shuffledUsers[11],
                    playerResult = if (isDraw) PlayerResult.DRAW else PlayerResult.LOSS,
                    team = MatchTeam.B
                ),
            ),
            start = start
        ))
        return match
    }
}

private val activeGroup1UserIds = listOf(
    UserId("user-id-1"),
    UserId("user-id-3"),
    UserId("user-id-4"),
    UserId("user-id-5"),
    UserId("user-id-6"),
    UserId("user-id-7"),
    UserId("user-id-8"),
    UserId("user-id-9"),
    UserId("user-id-10"),
    UserId("user-id-11"),
    UserId("user-id-12"),
    UserId("user-id-13"),
    UserId("user-id-14"),
    UserId("user-id-15"),
    UserId("user-id-16"),
    UserId("user-id-17"),
    UserId("user-id-18"),
)

private val allGroup1UserIds = listOf(
    UserId("user-id-1"),
    UserId("user-id-2"),
    UserId("user-id-3"),
    UserId("user-id-4"),
    UserId("user-id-5"),
    UserId("user-id-6"),
    UserId("user-id-7"),
    UserId("user-id-8"),
    UserId("user-id-9"),
    UserId("user-id-10"),
    UserId("user-id-11"),
    UserId("user-id-12"),
    UserId("user-id-13"),
    UserId("user-id-14"),
    UserId("user-id-15"),
    UserId("user-id-16"),
    UserId("user-id-17"),
    UserId("user-id-18"),
    UserId("user-id-19"),
    UserId("user-id-20"),
)

private val oldMatches = listOf(
    Triple("match-id-1", 70L, true),
    Triple("match-id-2", 63L, false),
    Triple("match-id-3", 56L, false),
    Triple("match-id-4", 49L, false),
    Triple("match-id-5", 42L, false),
    Triple("match-id-6", 35L, false),
    Triple("match-id-7", 28L, false),
    Triple("match-id-8", 21L, false),
    Triple("match-id-9", 14L, false),
    Triple("match-id-10", 7L, true)
)