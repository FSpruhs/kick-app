package com.spruhs.kick_app.match.core.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.generateId
import com.spruhs.kick_app.match.api.PlayerAddedToCadreEvent
import com.spruhs.kick_app.match.api.PlayerDeregisteredEvent
import com.spruhs.kick_app.match.api.PlayerPlacedOnWaitingBenchEvent
import com.spruhs.kick_app.match.api.PlayerPriorityStrategyType
import java.time.LocalDateTime

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = FirstComeFirstServe::class, name = "FIRST_COME_FIRST_SERVE"),
    JsonSubTypes.Type(value = RoundRobin::class, name = "ROUND_ROBIN"),
    JsonSubTypes.Type(value = AttendanceBased::class, name = "ATTENDANCE_BASED"),
)
interface PlayerPriorityStrategy {

    fun addRegistration(userId: UserId,
                        registrationStatusType: RegistrationStatusType,
                        guests: Int = 0,
                        match: MatchAggregate,
                        apply: (BaseEvent) -> Unit): List<BaseEvent>

    fun type(): PlayerPriorityStrategyType
}

class FirstComeFirstServe : PlayerPriorityStrategy {

    private val events: MutableList<BaseEvent> = mutableListOf()

    override fun addRegistration(
        userId: UserId,
        registrationStatusType: RegistrationStatusType,
        guests: Int,
        match: MatchAggregate,
        apply: (BaseEvent) -> Unit
    ): List<BaseEvent> {
        events.clear()
        startAddingRegistration(userId, registrationStatusType, guests, match) {
            events.add(it)
            apply(it)
        }
        return events.toList().also { events.clear() }
    }

    private fun startAddingRegistration(
        userId: UserId,
        registrationStatusType: RegistrationStatusType,
        guests: Int,
        match: MatchAggregate,
        apply: (BaseEvent) -> Unit
    ) {
        require(match.start.isAfter(LocalDateTime.now())) {
            throw MatchStartTimeException(MatchId(match.aggregateId))
        }
        require(guests >= 0 && guests <= match.playerCount.maxPlayer.value / 2) {
            "Guest must be between 0 and ${match.playerCount.maxPlayer.value / 2}"
        }
        val currentPlayer = findPlayerRegistration(userId, match)
        if (currentPlayer == null) {
            handleFirstRegistration(userId, registrationStatusType, guests, match, apply)
            return
        }

        val newStatus = currentPlayer.status.updateStatus(registrationStatusType)
        if (newStatus == currentPlayer.status) {
            if (currentPlayer.guests != guests) {
                updateJustGuests(userId, guests, newStatus, match, apply)
            }
            return
        }

        handleNewStatus(currentPlayer, newStatus, guests, match, apply)

        if (shouldFillCadreFromWaitingBench(newStatus, match)) {
            fillCadreFromWaitingBench(match,apply)
        }
    }

    private fun fillCadreFromWaitingBench(match: MatchAggregate,apply: (BaseEvent) -> Unit) {
        match.waitingBench.sortBy { it.registrationTime }
        val openCadre = match.playerCount.maxPlayer.value - match.cadre.size
        for (registration in match.waitingBench
            .filter { it.status.getType() == RegistrationStatusType.REGISTERED }
            .filterIsInstance<RegisteredPlayer.MainPlayer>()
            .take(openCadre)) {
            apply(
                PlayerAddedToCadreEvent(
                    match.aggregateId,
                    registration.userId,
                    registration.status.getType().name,
                    registration.guests,
                ),
            )
        }

        val newOpenCadre = match.playerCount.maxPlayer.value - match.cadre.size
        for (registration in match.waitingBench
            .filter { it.status.getType() == RegistrationStatusType.REGISTERED }
            .filterIsInstance<RegisteredPlayer.GuestPlayer>()
            .take(newOpenCadre)) {
            apply(
                PlayerAddedToCadreEvent(
                    match.aggregateId,
                    UserId(registration.guestId),
                    registration.status.getType().name,
                    0,
                    registration.guestOf,
                ),
            )
        }
    }

    private fun shouldFillCadreFromWaitingBench(newStatus: RegistrationStatus, match: MatchAggregate): Boolean =
        newStatus.getType() in listOf(RegistrationStatusType.DEREGISTERED, RegistrationStatusType.CANCELLED) &&
            !isCadreFull(match) &&
            isPlayerWaiting(match)

    private fun isPlayerWaiting(match: MatchAggregate): Boolean = match.waitingBench.any { it.status.getType() == RegistrationStatusType.REGISTERED }

    private fun handleNewStatus(
        currentPlayer: RegisteredPlayer.MainPlayer,
        newStatus: RegistrationStatus,
        guests: Int,
        match: MatchAggregate,
        apply: (BaseEvent) -> Unit
    ) {
        when (newStatus) {
            is RegistrationStatus.Registered ->
                handlePlayerRegistration(
                    currentPlayer.userId,
                    RegistrationStatusType.REGISTERED,
                    guests,
                    match,
                    apply
                )

            is RegistrationStatus.Deregistered ->
                handlePlayerDeregistration(
                    currentPlayer.userId,
                    RegistrationStatusType.DEREGISTERED,
                    match,
                    apply
                )

            is RegistrationStatus.Cancelled -> handlePlayerCancelled(currentPlayer.userId, RegistrationStatusType.CANCELLED, match, apply)
            is RegistrationStatus.Added -> handlePlayerAdded(currentPlayer.userId, RegistrationStatusType.ADDED, guests, match, apply)
        }
    }

    private fun handlePlayerCancelled(
        userId: UserId,
        status: RegistrationStatusType,
        match: MatchAggregate,
        apply: (BaseEvent) -> Unit
    ) {
        apply(PlayerPlacedOnWaitingBenchEvent(match.aggregateId, userId, status.name))
        val cadreGuests = match.cadre.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }
        val waitingBenchGuests = match.waitingBench.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }

        (cadreGuests + waitingBenchGuests).forEach { guest ->
            apply(PlayerPlacedOnWaitingBenchEvent(match.aggregateId, UserId(guest.guestId), status.name, 0, userId))
        }
    }

    private fun handlePlayerAdded(
        userId: UserId,
        status: RegistrationStatusType,
        guests: Int,
        match: MatchAggregate,
        apply: (BaseEvent) -> Unit
    ) {
        apply(PlayerAddedToCadreEvent(match.aggregateId, userId, status.name, guests))

        val cadreGuests = match.cadre.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }
        val waitingBenchGuests = match.waitingBench.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }

        (cadreGuests + waitingBenchGuests).forEach { guest ->
            apply(PlayerAddedToCadreEvent(match.aggregateId, UserId(guest.guestId), status.name, 0, userId))
        }
    }

    private fun updateJustGuests(
        userId: UserId,
        guests: Int,
        status: RegistrationStatus,
        match: MatchAggregate,
        apply: (BaseEvent) -> Unit
    ) {
        if (status != RegistrationStatus.Registered) return

        val cadreGuests = match.cadre.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }
        val waitingBenchGuests =
            match.waitingBench.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }

        val totalGuests = cadreGuests.size + waitingBenchGuests.size
        if (guests < totalGuests) {
            val guestsToRemove = totalGuests - guests
            repeat(guestsToRemove) {
                if (waitingBenchGuests.isNotEmpty()) {
                    val guestToRemove = waitingBenchGuests.first()
                    apply(PlayerDeregisteredEvent(match.aggregateId, UserId(guestToRemove.guestId), status.getType().name, 0, userId))
                } else if (cadreGuests.isNotEmpty()) {
                    val guestToRemove = cadreGuests.first()
                    apply(PlayerDeregisteredEvent(match.aggregateId, UserId(guestToRemove.guestId), status.getType().name, 0, userId))
                }
            }
        } else if (guests > totalGuests) {
            val guestsToAdd = guests - totalGuests
            repeat(guestsToAdd) {
                if (!isCadreFull(match)) {
                    apply(
                        PlayerAddedToCadreEvent(
                            match.aggregateId,
                            UserId(generateId()),
                            RegistrationStatusType.REGISTERED.name,
                            0,
                            userId,
                        ),
                    )
                } else {
                    apply(
                        PlayerPlacedOnWaitingBenchEvent(
                            match.aggregateId,
                            UserId(generateId()),
                            RegistrationStatusType.REGISTERED.name,
                            0,
                            userId,
                        ),
                    )
                }
            }
        }
    }

    private fun isCadreFull(match: MatchAggregate): Boolean = match.cadre.size >= match.playerCount.maxPlayer.value

    private fun findPlayerRegistration(userId: UserId, match: MatchAggregate): RegisteredPlayer.MainPlayer? =
        (match.cadre + match.waitingBench + match.deregistered)
            .filterIsInstance<RegisteredPlayer.MainPlayer>()
            .find { it.userId == userId }

    private fun handleFirstRegistration(
        userId: UserId,
        registrationStatusType: RegistrationStatusType,
        guests: Int,
        match: MatchAggregate,
        apply: (BaseEvent) -> Unit
    ) {
        when (registrationStatusType) {
            RegistrationStatusType.DEREGISTERED ->
                handlePlayerDeregistration(
                    userId,
                    RegistrationStatusType.DEREGISTERED,
                    match,
                    apply
                )

            RegistrationStatusType.REGISTERED ->
                handlePlayerRegistration(userId, RegistrationStatusType.REGISTERED, guests, match, apply)

            RegistrationStatusType.CANCELLED -> return
            RegistrationStatusType.ADDED -> return
        }
    }

    private fun handlePlayerDeregistration(
        userId: UserId,
        status: RegistrationStatusType,
        match: MatchAggregate,
        apply: (BaseEvent) -> Unit
    ) {
        apply(PlayerDeregisteredEvent(match.aggregateId, userId, status.name))

        val cadreGuests = match.cadre.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }
        val waitingBenchGuests = match.waitingBench.filterIsInstance<RegisteredPlayer.GuestPlayer>().filter { it.guestOf == userId }

        (cadreGuests + waitingBenchGuests).forEach { guest ->
            apply(PlayerDeregisteredEvent(match.aggregateId, UserId(guest.guestId), status.name, 0, userId))
        }
    }

    private fun handlePlayerRegistration(
        userId: UserId,
        status: RegistrationStatusType,
        guests: Int,
        match: MatchAggregate,
        apply: (BaseEvent) -> Unit
    ) {
        val totalPlayers = 1 + guests
        var matchCapacity = match.playerCount.maxPlayer.value - match.cadre.size

        if (matchCapacity == 0 && matchContainsGuests(match)) {
            match.cadre.sortBy { it.registrationTime }
            val lastGuest = match.cadre.filterIsInstance<RegisteredPlayer.GuestPlayer>().last()
            apply(PlayerPlacedOnWaitingBenchEvent(match.aggregateId, UserId(lastGuest.guestId), status.name, 0, lastGuest.guestOf))
            matchCapacity += 1
        }

        val cadreSlots = totalPlayers.coerceAtMost(matchCapacity).coerceAtLeast(0)
        var benchSlots = totalPlayers - cadreSlots

        if (cadreSlots > 0) {
            apply(PlayerAddedToCadreEvent(match.aggregateId, userId, status.name, guests))
        } else {
            apply(PlayerPlacedOnWaitingBenchEvent(match.aggregateId, userId, status.name, guests))
            benchSlots -= 1
        }

        repeat(cadreSlots - 1) {
            apply(PlayerAddedToCadreEvent(match.aggregateId, UserId(generateId()), status.name, 0, userId))
        }
        repeat(benchSlots) {
            apply(PlayerPlacedOnWaitingBenchEvent(match.aggregateId, UserId(generateId()), status.name, 0, userId))
        }
    }

    private fun matchContainsGuests(match: MatchAggregate): Boolean = match.cadre.any { it is RegisteredPlayer.GuestPlayer }

    override fun type(): PlayerPriorityStrategyType =
        PlayerPriorityStrategyType.FIRST_COME_FIRST_SERVE
}

class RoundRobin : PlayerPriorityStrategy {
    override fun addRegistration(
        userId: UserId,
        registrationStatusType: RegistrationStatusType,
        guests: Int,
        match: MatchAggregate,
        apply: (BaseEvent) -> Unit
    ): List<BaseEvent> {
        TODO("Not yet implemented")
    }

    override fun type(): PlayerPriorityStrategyType =
        PlayerPriorityStrategyType.ROUND_ROBIN
}

class AttendanceBased : PlayerPriorityStrategy {
    override fun addRegistration(
        userId: UserId,
        registrationStatusType: RegistrationStatusType,
        guests: Int,
        match: MatchAggregate,
        apply: (BaseEvent) -> Unit
    ): List<BaseEvent> {
        TODO("Not yet implemented")
    }

    override fun type(): PlayerPriorityStrategyType = PlayerPriorityStrategyType.ATTENDANCE_BASED
}
