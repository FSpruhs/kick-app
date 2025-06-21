package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.group.core.adapter.secondary.GroupDocument
import com.spruhs.kick_app.view.core.service.GroupProjection
import com.spruhs.kick_app.view.core.service.GroupProjectionRepository
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class GroupProjectionMongoDB : GroupProjectionRepository {
    override suspend fun findById(groupId: GroupId): GroupProjection {
        TODO("Not yet implemented")
    }

    override suspend fun save(groupProjection: GroupProjection) {
        TODO("Not yet implemented")
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