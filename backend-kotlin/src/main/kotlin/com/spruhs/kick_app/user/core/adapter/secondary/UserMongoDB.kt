package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.user.core.domain.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
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
    val password: String,
    val groups: List<String>
)

@Service
class UserPersistenceAdapter(val repository: UserRepository) : UserPersistencePort {
    override fun save(user: User) {
        repository.save(user.toDocument())
    }

    override fun existsByEmail(email: Email): Boolean {
        return repository.existsByEmail(email.value)
    }

    override fun findById(userId: UserId): User? {
        return repository.findById(userId.value).map { it.toDmain() }.orElse(null)
    }
}

@Repository
interface UserRepository : MongoRepository<UserDocument, String> {
    fun existsByEmail(email: String): Boolean
}

private fun User.toDocument() = UserDocument(
    id = id.value,
    firstName = fullName.firstName.value,
    lastName = fullName.lastName.value,
    nickName = nickName.value,
    email = email.value,
    password = password.value,
    groups = groups.map { it.value }
)

private fun UserDocument.toDmain() = User(
    UserId(id),
    FullName(FirstName(firstName), LastName(lastName)),
    NickName(nickName),
    Email(email),
    Password(password),
    groups.map { GroupId(it) }
)