package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.domain.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

@Document(collection = "users")
data class UserDocument(
    @Id
    val id: String,
    val firstName: String,
    val lastName: String,
    val nickName: String,
    val email: String,
    val groups: List<String>
)

@Service
class UserPersistenceAdapter(private val repository: UserRepository) : UserPersistencePort {
    override fun save(user: User) {
        repository.save(user.toDocument())
    }

    override fun existsByEmail(email: Email): Boolean {
        return repository.existsByEmail(email.value)
    }

    override fun findById(userId: UserId): User? {
        return repository.findById(userId.value).map { it.toDomain() }.orElse(null)
    }

    override fun findByIds(userIds: List<UserId>): List<User> {
        return repository.findAllById(userIds.map { it.value }).map { it.toDomain() }
    }

    override fun findAll(exceptGroupId: GroupId?): List<User> {
        return if (exceptGroupId != null) {
            repository.findByGroupNotContaining(exceptGroupId.value).map { it.toDomain() }
        } else {
            repository.findAll().map { it.toDomain() }
        }
    }
}

@Repository
interface UserRepository : MongoRepository<UserDocument, String> {
    fun existsByEmail(email: String): Boolean

    @Query("{ 'groups': { \$nin: [?0] } }")
    fun findByGroupNotContaining(group: String): List<UserDocument>
}

private fun User.toDocument() = UserDocument(
    id = id.value,
    firstName = fullName.firstName.value,
    lastName = fullName.lastName.value,
    nickName = nickName.value,
    email = email.value,
    groups = groups.map { it.value }
)

private fun UserDocument.toDomain() = User(
    UserId(id),
    FullName(FirstName(firstName), LastName(lastName)),
    NickName(nickName),
    Email(email),
    groups.map { GroupId(it) }
)