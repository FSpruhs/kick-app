package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.view.core.service.GroupNameListEntry
import com.spruhs.kick_app.view.core.service.GroupNameListProjection
import com.spruhs.kick_app.view.core.service.GroupNameListProjectionRepository
import com.spruhs.kick_app.view.core.service.GroupProjection
import com.spruhs.kick_app.view.core.service.GroupProjectionRepository
import com.spruhs.kick_app.view.core.service.PlayerProjection
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Document(collection = "groups")
data class GroupDocument(
    @Id
    val id: String,
    val name: String,
    val players: List<PlayerDocument>,
)

@Document(collection = "group_name_list")
data class GroupNameListDocument(
    @Id
    val groupId: String,
    val playerEntries: List<GroupNameListEntryDocument>
)

data class GroupNameListEntryDocument(
    val userId: String,
    val name: String,
    val imageUrl: String? = null,
)

data class PlayerDocument(
    val id: String,
    val status: String,
    val role: String,
    val email: String,
)

@Service
class GroupProjectionMongoDB(private val repository: GroupRepository) : GroupProjectionRepository {
    override suspend fun findById(groupId: GroupId): GroupProjection? =
        repository.findById(groupId.value)
            .awaitFirstOrNull()
            ?.toProjection()

    override suspend fun save(groupProjection: GroupProjection) {
        repository.save(groupProjection.toDocument()).awaitSingle()
    }
}

@Service
class GroupNameListMongoDB(private val repository: GroupNameListRepository) : GroupNameListProjectionRepository {
    override suspend fun findByGroupId(groupId: GroupId): GroupNameListProjection? =
        repository.findById(groupId.value)
            .awaitFirstOrNull()
            ?.toProjection()

    override suspend fun save(groupNameList: GroupNameListProjection) {
        repository.save(groupNameList.toDocument()).awaitSingle()
    }

    override suspend fun findByUserId(userId: UserId): List<GroupNameListProjection> =
        repository.findByPlayerEntriesUserId(userId.value)
            .map { it.toProjection() }
            .collectList()
            .awaitSingle()

}

@Repository
interface GroupRepository : ReactiveMongoRepository<GroupDocument, String>

@Repository
interface GroupNameListRepository : ReactiveMongoRepository<GroupNameListDocument, String> {
    fun findByPlayerEntriesUserId(userId: String): Flux<GroupNameListDocument>
}

private fun GroupDocument.toProjection() = GroupProjection(
        id = GroupId(id),
        name = name,
        players = players.map {
            PlayerProjection(
                id = UserId(it.id),
                status = PlayerStatusType.valueOf(it.status),
                role = PlayerRole.valueOf(it.role),
                email = it.email,
            )
        }
    )

private fun GroupProjection.toDocument() = GroupDocument(
        id = id.value,
        name = name,
        players = players.map {
            PlayerDocument(
                id = it.id.value,
                status = it.status.name,
                role = it.role.name,
                email = it.email,
            )
        }
    )

private fun GroupNameListDocument.toProjection() = GroupNameListProjection(
        groupId = GroupId(groupId),
        players = playerEntries.map {
            GroupNameListEntry(
                userId = UserId(it.userId),
                name = it.name,
                imageUrl = it.imageUrl
            )
        }
    )

private fun GroupNameListProjection.toDocument() = GroupNameListDocument(
        groupId = groupId.value,
        playerEntries = players.map {
            GroupNameListEntryDocument(
                userId = it.userId.value,
                name = it.name,
                imageUrl = it.imageUrl
            )
        }
    )