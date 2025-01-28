package com.spruhs.kick_app.group.core.adapter.secondary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.domain.*
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

@Service
class GroupPersistenceAdapter(val repository: GroupRepository) : GroupPersistencePort {
    override fun save(group: Group) {
        repository.save(group.toDocument())
    }

    override fun findById(groupId: GroupId): Group? {
        return repository.findById(groupId.value).map { it.toDomain() }.orElse(null)
    }

    override fun findByPlayer(userId: UserId): List<Group> {
        return repository.findAllByPlayersIdContains(userId.value).map { it.toDomain() }
    }
}

@Document(collection = "groups")
data class GroupDocument(
    val id: String,
    val name: String,
    val players: List<PlayerDocument>,
    val invitedUsers: List<String>
)

data class PlayerDocument(
    val id: String,
    val status: String,
    val role: String
)

@Repository
interface GroupRepository : MongoRepository<GroupDocument, String> {
    fun findAllByPlayersIdContains(userId: String): List<GroupDocument>
}

private fun Group.toDocument() = GroupDocument(
    id = id.value,
    name = name.value,
    players = players.map { PlayerDocument(it.id.value, it.status.name, it.role.name) },
    invitedUsers = invitedUsers.map { it.value }
)

private fun GroupDocument.toDomain() = Group(
    id = GroupId(id),
    name = Name(name),
    players = players.map { Player(UserId(it.id), PlayerStatus.valueOf(it.status), PlayerRole.valueOf(it.role)) },
    invitedUsers = invitedUsers.map { UserId(it) }
)