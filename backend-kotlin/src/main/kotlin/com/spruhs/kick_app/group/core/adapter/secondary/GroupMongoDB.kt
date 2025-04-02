package com.spruhs.kick_app.group.core.adapter.secondary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.domain.*
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class GroupPersistenceAdapter(val repository: GroupRepository) : GroupPersistencePort {
    override suspend fun save(group: Group) {
        repository.save(group.toDocument()).awaitFirstOrNull()
    }

    override suspend fun findById(groupId: GroupId): Group? {
        return repository.findById(groupId.value).map { it.toDomain() }.awaitFirstOrNull()
    }

    override suspend fun findByPlayer(userId: UserId): List<Group> {
        return repository.findAllByPlayersIdContains(userId.value).collectList().awaitFirst().map { it.toDomain() }
    }
}

private fun Group.toDocument() = GroupDocument(
    id = id.value,
    name = name.value,
    players = players.map { PlayerDocument(it.id.value, it.status.type().name, it.role.name) },
    invitedUsers = invitedUsers.map { it.value }
)

private fun GroupDocument.toDomain() = Group(
    id = GroupId(id),
    name = Name(name),
    players = players.map { Player(UserId(it.id), PlayerStatusType.valueOf(it.status).toStatus(), PlayerRole.valueOf(it.role)) },
    invitedUsers = invitedUsers.map { UserId(it) }
)