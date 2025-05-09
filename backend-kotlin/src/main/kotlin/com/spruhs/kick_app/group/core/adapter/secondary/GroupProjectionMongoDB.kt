package com.spruhs.kick_app.group.core.adapter.secondary

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UnknownEventTypeException
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.api.*
import com.spruhs.kick_app.group.core.domain.*
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Document(collection = "groups")
data class GroupDocument(
    val id: String,
    var name: String,
    var players: List<PlayerDocument>,
)

data class PlayerDocument(
    val id: String,
    var status: String,
    var role: String
)

@Service
class GroupProjectionMongoAdapter(
    private val repository: GroupRepository,
) : GroupProjectionPort {
    override suspend fun whenEvent(event: BaseEvent) {
        when (event) {
            is GroupCreatedEvent -> handleGroupCreatedEvent(event)
            is GroupNameChangedEvent -> handleGroupNameChangedEvent(event)
            is PlayerEnteredGroupEvent -> handlePlayerEnteredGroupEvent(event)

            is PlayerPromotedEvent -> handlePlayerRoleEvent(
                GroupId(event.aggregateId),
                event.userId,
                PlayerRole.ADMIN
            )

            is PlayerDowngradedEvent -> handlePlayerRoleEvent(
                GroupId(event.aggregateId),
                event.userId,
                PlayerRole.PLAYER
            )

            is PlayerActivatedEvent -> handlePlayerStatusEvent(
                GroupId(event.aggregateId),
                event.userId,
                PlayerStatusType.ACTIVE
            )

            is PlayerDeactivatedEvent -> handlePlayerStatusEvent(
                GroupId(event.aggregateId),
                event.userId,
                PlayerStatusType.INACTIVE
            )

            is PlayerRemovedEvent -> handlePlayerStatusEvent(
                GroupId(event.aggregateId),
                event.userId,
                PlayerStatusType.REMOVED
            )

            is PlayerLeavedEvent -> handlePlayerStatusEvent(
                GroupId(event.aggregateId),
                event.userId,
                PlayerStatusType.LEAVED
            )

            else -> {
                throw UnknownEventTypeException(event)
            }
        }
    }

    private fun newPlayerDocument(userId: UserId) = PlayerDocument(
        id = userId.value,
        status = PlayerStatusType.ACTIVE.name,
        role = PlayerRole.PLAYER.name
    )

    private suspend fun handleGroupCreatedEvent(event: GroupCreatedEvent) {
        GroupDocument(
            id = event.aggregateId,
            name = event.name,
            players = listOf(newPlayerDocument(event.userId)),
        ).also {
            repository.save(it).awaitFirstOrNull()
        }
    }

    private suspend fun fetchGroup(groupId: String): GroupDocument =
        repository
            .findById(groupId)
            .awaitFirstOrNull()
            ?: throw GroupNotFoundException(GroupId(groupId))


    private suspend fun handleGroupNameChangedEvent(event: GroupNameChangedEvent) {
        fetchGroup(event.aggregateId).let {
            it.name = event.name
            repository.save(it).awaitSingle()
        }
    }

    private suspend fun handlePlayerEnteredGroupEvent(event: PlayerEnteredGroupEvent) {
        fetchGroup(event.aggregateId).let {
            it.players += newPlayerDocument(event.userId)
            repository.save(it).awaitSingle()
        }
    }

    private suspend fun handlePlayerRoleEvent(groupId: GroupId, userId: UserId, role: PlayerRole) {
        fetchGroup(groupId.value).let {
            it.players.find { player -> player.id == userId.value }?.role = role.name
            repository.save(it).awaitSingle()
        }
    }

    private suspend fun handlePlayerStatusEvent(groupId: GroupId, userId: UserId, status: PlayerStatusType) {
        fetchGroup(groupId.value).let {
            it.players.find { player -> player.id == userId.value }?.status = status.name
            repository.save(it).awaitSingle()
        }
    }

    override suspend fun findById(groupId: GroupId): GroupProjection? =
        repository.findById(groupId.value)
            .map { it.toProjection() }
            .awaitFirstOrNull()


    override suspend fun findByPlayer(userId: UserId): List<GroupProjection> =
        repository.findAllByPlayersIdContains(userId.value)
            .collectList()
            .awaitFirst()
            .map { it.toProjection() }
}

@Repository
interface GroupRepository : ReactiveMongoRepository<GroupDocument, String> {
    fun findAllByPlayersIdContains(userId: String): Flux<GroupDocument>
}

private fun GroupDocument.toProjection() = GroupProjection(
    id = GroupId(id),
    name = Name(name),
    players = players.map {
        PlayerProjection(
            UserId(it.id),
            PlayerStatusType.valueOf(it.status),
            PlayerRole.valueOf(it.role)
        )
    },
)
