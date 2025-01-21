package com.spruhs.kick_app.group.core.adapter.secondary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.domain.Group
import com.spruhs.kick_app.group.core.domain.GroupPersistencePort
import com.spruhs.kick_app.group.core.domain.Name
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

@Service
class GroupPersistenceAdapter(val repository: GroupRepository): GroupPersistencePort {
    override fun save(group: Group) {
        repository.save(group.toDocument())
    }

    override fun findById(groupId: GroupId): Group? {
        return repository.findById(groupId.value).orElse(null).toDomain()
    }

    override fun findByPlayer(userId: UserId): List<Group> {
        return repository.findAllByPlayersContains(userId.value).map { it.toDomain() }
    }
}

@Document(collection = "groups")
data class GroupDocument(
    val id: String,
    val name: String,
    val players: List<String>,
    val invitedUsers: List<String>
)

@Repository
interface GroupRepository : MongoRepository<GroupDocument, String> {
    fun findAllByPlayersContains(userId: String): List<GroupDocument>
}

private fun Group.toDocument() = GroupDocument(
    id = id.value,
    name = name.value,
    players = players.map { it.value },
    invitedUsers = invitedUsers.map { it.value }
)

private fun GroupDocument.toDomain() = Group(
    id = GroupId(id),
    name = Name(name),
    players = players.map { UserId(it) },
    invitedUsers = invitedUsers.map { UserId(it) }
)