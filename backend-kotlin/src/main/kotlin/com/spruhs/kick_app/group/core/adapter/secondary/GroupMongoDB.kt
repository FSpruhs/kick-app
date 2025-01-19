package com.spruhs.kick_app.group.core.adapter.secondary

import com.spruhs.kick_app.group.core.domain.Group
import com.spruhs.kick_app.group.core.domain.GroupPersistencePort
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

@Service
class GroupPersistenceAdapter(val repository: GroupRepository): GroupPersistencePort {
    override fun save(group: Group) {
        repository.save(group.toDocument())
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
interface GroupRepository : MongoRepository<GroupDocument, String>

private fun Group.toDocument() = GroupDocument(
    id = id.value,
    name = name.value,
    players = players.map { it.value },
    invitedUsers = invitedUsers.map { it.value }
)