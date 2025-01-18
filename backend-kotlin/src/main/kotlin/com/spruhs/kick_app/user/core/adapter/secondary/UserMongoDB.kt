package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.user.core.domain.Email
import com.spruhs.kick_app.user.core.domain.User
import com.spruhs.kick_app.user.core.domain.UserPersistencePort
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