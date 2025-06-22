package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.domain.Name
import com.spruhs.kick_app.view.core.service.GroupProjection
import com.spruhs.kick_app.view.core.service.GroupProjectionRepository
import com.spruhs.kick_app.view.core.service.PlayerProjection
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

@Service
class GroupProjectionMongoDB(private val repository: GroupRepository) : GroupProjectionRepository {
    override suspend fun findById(groupId: GroupId): GroupProjection? {
        return repository.findById(groupId.value).awaitFirstOrNull()?.toProjection()
    }

    override suspend fun save(groupProjection: GroupProjection) {
        repository.save(groupProjection.toDocument()).awaitSingle()
    }
}

@Document(collection = "groups")
data class GroupDocument(
    val id: String,
    val name: String,
    val players: List<PlayerDocument>,
)

data class PlayerDocument(
    val id: String,
    val status: String,
    val role: String,
    val avatarUrl: String? = null,
    val email: String,
)

@Repository
interface GroupRepository : ReactiveMongoRepository<GroupDocument, String>

private fun GroupDocument.toProjection() = GroupProjection(
        id = GroupId(id),
        name = Name(name),
        players = players.map {
            PlayerProjection(
                id = UserId(it.id),
                status = PlayerStatusType.valueOf(it.status),
                role = PlayerRole.valueOf(it.role),
                avatarUrl = it.avatarUrl,
                email = it.email,
            )
        }
    )

private fun GroupProjection.toDocument() = GroupDocument(
        id = id.value,
        name = name.value,
        players = players.map {
            PlayerDocument(
                id = it.id.value,
                status = it.status.name,
                role = it.role.name,
                avatarUrl = it.avatarUrl,
                email = it.email,
            )
        }
    )