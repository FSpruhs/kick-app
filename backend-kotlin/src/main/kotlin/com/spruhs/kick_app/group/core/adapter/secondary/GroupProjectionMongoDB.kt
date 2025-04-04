package com.spruhs.kick_app.group.core.adapter.secondary

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UnknownEventTypeException
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.api.*
import com.spruhs.kick_app.group.core.domain.*
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
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
    var invitedUsers: List<String>
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
            is PlayerPromotedEvent -> handlePlayerRoleEvent(UserId(event.userId), PlayerRole.ADMIN)
            is PlayerDowngradedEvent -> handlePlayerRoleEvent(UserId(event.userId), PlayerRole.PLAYER)
            is PlayerActivatedEvent -> handlePlayerStatusEvent(UserId(event.userId), PlayerStatusType.ACTIVE)
            is PlayerDeactivatedEvent -> handlePlayerStatusEvent(UserId(event.userId), PlayerStatusType.INACTIVE)
            is PlayerRemovedEvent -> handlePlayerStatusEvent(UserId(event.userId), PlayerStatusType.REMOVED)
            is PlayerLeavedEvent -> handlePlayerStatusEvent(UserId(event.userId), PlayerStatusType.LEAVED)

            else -> { throw UnknownEventTypeException(event) }
        }
    }

    private suspend fun handleGroupCreatedEvent(event: GroupCreatedEvent) {
        GroupDocument(
            id = event.aggregateId,
            name = event.name,
            players = listOf(PlayerDocument(event.userId, PlayerStatusType.ACTIVE.name, PlayerRole.ADMIN.name)),
            invitedUsers = emptyList()
        ).also {
            repository.save(it).awaitFirstOrNull()
        }
    }

    private suspend fun handleGroupNameChangedEvent(event: GroupNameChangedEvent) {
        repository.findById(event.aggregateId).awaitFirstOrNull()?.let {
            it.name = event.name
            repository.save(it)
        } ?: throw GroupNotFoundException(GroupId(event.aggregateId))
    }

    private suspend fun handlePlayerEnteredGroupEvent(event: PlayerEnteredGroupEvent) {
        repository.findById(event.aggregateId).awaitFirstOrNull()?.let {
            it.players += PlayerDocument(event.userId, PlayerStatusType.ACTIVE.name, PlayerRole.PLAYER.name)
            it.invitedUsers -= event.userId
            repository.save(it)
        } ?: throw GroupNotFoundException(GroupId(event.aggregateId))
    }

    private suspend fun handlePlayerRoleEvent(userId: UserId, role: PlayerRole) {
        repository.findById(userId.value).awaitFirstOrNull()?.let {
            it.players.find { player -> player.id == userId.value }?.role = role.name
            repository.save(it)
        } ?: throw GroupNotFoundException(GroupId(userId.value))
    }

    private suspend fun handlePlayerStatusEvent(userId: UserId, status: PlayerStatusType) {
        repository.findById(userId.value).awaitFirstOrNull()?.let {
            it.players.find { player -> player.id == userId.value }?.status = status.name
            repository.save(it)
        } ?: throw GroupNotFoundException(GroupId(userId.value))
    }

    override suspend fun findById(groupId: GroupId): GroupProjection? {
        return repository.findById(groupId.value).map { it.toProjection() }.awaitFirstOrNull()
    }

    override suspend fun findByPlayer(userId: UserId): List<GroupProjection> {
        return repository.findAllByPlayersIdContains(userId.value).collectList().awaitFirst().map { it.toProjection() }
    }
}


@Repository
interface GroupRepository : ReactiveMongoRepository<GroupDocument, String> {
    fun findAllByPlayersIdContains(userId: String): Flux<GroupDocument>
}

private fun GroupDocument.toProjection() = GroupProjection(
    id = GroupId(id),
    name = Name(name),
    players = players.map { PlayerProjection(UserId(it.id), PlayerStatusType.valueOf(it.status), PlayerRole.valueOf(it.role)) },
    invitedUsers = invitedUsers.map { UserId(it) }
)
