package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.UserImageId
import com.spruhs.kick_app.view.core.service.UserGroupProjection
import com.spruhs.kick_app.view.core.service.UserProjection
import com.spruhs.kick_app.view.core.service.UserProjectionRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Document(collection = "users")
data class UserDocument(
    @Id
    val id: String,
    var nickName: String,
    val email: String,
    var userImageId: String? = null,
    var groups: List<UserGroupDocument>
)

@Document
data class UserGroupDocument(
    @Id
    val id: String,
    var name: String,
    var userRole: String,
    var userStatus: String,
    var lastMatch: LocalDateTime? = null,
)

@Service
class UserProjectionMongoDB(
    private val repository: UserRepository,
) : UserProjectionRepository {

    override suspend fun getUser(userId: UserId): UserProjection? =
        repository.findById(userId.value)
            .awaitFirstOrNull()
            ?.toProjection()

    override suspend fun save(userProjection: UserProjection) {
        repository.save(userProjection.toDocument()).awaitSingle()
    }

    override suspend fun saveAll(userProjection: List<UserProjection>) {
        repository.saveAll(userProjection.map { it.toDocument() }).collectList().awaitSingle()
    }

    override suspend fun findByGroupId(groupId: GroupId): List<UserProjection> {
        return repository.findByGroupsId(groupId.value)
            .collectList()
            .awaitSingle()
            .map { it.toProjection() }
    }

    override suspend fun existsByEmail(email: String): Boolean {
        return repository.existsByEmail(email).awaitSingle()
    }
}

@Repository
interface UserRepository : ReactiveMongoRepository<UserDocument, String> {
    fun existsByEmail(email: String): Mono<Boolean>

    fun findByGroupsId(groupId: String): Flux<UserDocument>
}

private fun UserDocument.toProjection() = UserProjection(
    id = UserId(this.id),
    nickName = this.nickName,
    email = this.email,
    userImageId = this.userImageId?.let { UserImageId(it) },
    groups = this.groups.map {
        UserGroupProjection(
            id = GroupId(it.id),
            name = it.name,
            userStatus = PlayerStatusType.valueOf(it.userStatus),
            userRole = PlayerRole.valueOf(it.userRole),
            lastMatch = it.lastMatch
        )
    }
)

private fun UserProjection.toDocument() = UserDocument(
    id = this.id.value,
    nickName = this.nickName,
    email = this.email,
    userImageId = this.userImageId?.value,
    groups = this.groups.map {
        UserGroupDocument(
            id = it.id.value,
            name = it.name,
            userRole = it.userRole.name,
            userStatus = it.userStatus.name,
            lastMatch = it.lastMatch
        )
    }
)