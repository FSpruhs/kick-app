package com.spruhs.kick_app.match.core.adapter.primary

import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.match.core.adapter.secondary.MatchDocument
import com.spruhs.kick_app.match.core.adapter.secondary.MatchRepository
import com.spruhs.kick_app.match.core.adapter.secondary.ParticipatingPlayerDocument
import com.spruhs.kick_app.match.core.adapter.secondary.RegisteredPlayerDocument
import com.spruhs.kick_app.match.core.domain.MatchStatus
import com.spruhs.kick_app.match.core.domain.RegistrationStatus
import com.spruhs.kick_app.match.core.domain.Result
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@Profile("dev")
class MatchImporter(private val matchRepository: MatchRepository) {

    @Value("\${app.load-default-data}")
    private var loadDefaultData: Boolean = false

    private val log = getLogger(this::class.java)

    @PostConstruct
    fun loadData() {
        if (!loadDefaultData) {
            return
        }

        matchRepository.deleteAll()
        matchRepository.saveAll(defaultMatches)

        log.info("Default match data loaded")
    }
}

private val defaultMatches = listOf(
    MatchDocument(
        id = "match-1",
        groupId = "donnerstags-kick",
        start = LocalDateTime.now().minusWeeks(4L).toString(),
        status = MatchStatus.FINISHED.name,
        location = "Olympia Halle",
        minPlayer = 8,
        maxPlayer = 12,
        result = Result.WINNER_TEAM_A.name,
        participatingPlayers = listOf(
            ParticipatingPlayerDocument(
                userId = "user-id-2",
                team = "A"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-3",
                team = "A"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-4",
                team = "A"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-5",
                team = "A"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-6",
                team = "B"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-17",
                team = "B"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-8",
                team = "B"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-9",
                team = "B"
            )
        ),
        registeredPlayers = emptyList()
    ),
    MatchDocument(
        id = "match-2",
        groupId = "donnerstags-kick",
        start = LocalDateTime.now().minusWeeks(3L).toString(),
        status = MatchStatus.FINISHED.name,
        location = "Olympia Halle",
        minPlayer = 8,
        maxPlayer = 12,
        result = Result.WINNER_TEAM_B.name,
        participatingPlayers = listOf(
            ParticipatingPlayerDocument(
                userId = "user-id-2",
                team = "A"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-3",
                team = "A"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-4",
                team = "A"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-5",
                team = "A"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-6",
                team = "B"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-17",
                team = "B"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-8",
                team = "B"
            ),
            ParticipatingPlayerDocument(
                userId = "user-id-9",
                team = "B"
            )
        ),
        registeredPlayers = emptyList()
    ),
    MatchDocument(
        id = "match-3",
        groupId = "donnerstags-kick",
        start = LocalDateTime.now().minusWeeks(2L).toString(),
        status = MatchStatus.CANCELLED.name,
        location = "Olympia Halle",
        minPlayer = 8,
        maxPlayer = 12,
        result = Result.WINNER_TEAM_A.name,
        participatingPlayers = emptyList(),
        registeredPlayers = emptyList()
    ), MatchDocument(
        id = "match-4",
        groupId = "donnerstags-kick",
        start = LocalDateTime.now().minusWeeks(1L).toString(),
        status = MatchStatus.ENTER_RESULT.name,
        location = "Olympia Halle",
        minPlayer = 8,
        maxPlayer = 12,
        result = Result.WINNER_TEAM_A.name,
        participatingPlayers = emptyList(),
        registeredPlayers = emptyList()
    ),
    MatchDocument(
        id = "match-5",
        groupId = "donnerstags-kick",
        start = LocalDateTime.now().plusWeeks(1L).toString(),
        status = MatchStatus.PLANNED.name,
        location = "Olympia Halle",
        minPlayer = 8,
        maxPlayer = 12,
        result = null,
        participatingPlayers = emptyList(),
        registeredPlayers = listOf(
            RegisteredPlayerDocument(
                userId = "da082e6e-b4c1-40a4-8144-9098a2d819d9",
                registrationTime = LocalDateTime.now().toString(),
                status = RegistrationStatus.DEREGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-2",
                registrationTime = LocalDateTime.now().toString(),
                status = RegistrationStatus.DEREGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-3",
                registrationTime = LocalDateTime.now().toString(),
                status = RegistrationStatus.REGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-4",
                registrationTime = LocalDateTime.now().plusHours(1).toString(),
                status = RegistrationStatus.REGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-5",
                registrationTime = LocalDateTime.now().plusHours(2).toString(),
                status = RegistrationStatus.REGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-6",
                registrationTime = LocalDateTime.now().plusHours(3).toString(),
                status = RegistrationStatus.REGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-17",
                registrationTime = LocalDateTime.now().plusHours(4).toString(),
                status = RegistrationStatus.REGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-8",
                registrationTime = LocalDateTime.now().plusHours(5).toString(),
                status = RegistrationStatus.REGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-9",
                registrationTime = LocalDateTime.now().plusHours(6).toString(),
                status = RegistrationStatus.REGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-18",
                registrationTime = LocalDateTime.now().toString(),
                status = RegistrationStatus.REGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-11",
                registrationTime = LocalDateTime.now().toString(),
                status = RegistrationStatus.REGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-12",
                registrationTime = LocalDateTime.now().toString(),
                status = RegistrationStatus.REGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-13",
                registrationTime = LocalDateTime.now().toString(),
                status = RegistrationStatus.REGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-14",
                registrationTime = LocalDateTime.now().toString(),
                status = RegistrationStatus.REGISTERED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-15",
                registrationTime = LocalDateTime.now().toString(),
                status = RegistrationStatus.ADDED.name
            ),
            RegisteredPlayerDocument(
                userId = "user-id-16",
                registrationTime = LocalDateTime.now().toString(),
                status = RegistrationStatus.CANCELLED.name
            ),
        )
    )
)