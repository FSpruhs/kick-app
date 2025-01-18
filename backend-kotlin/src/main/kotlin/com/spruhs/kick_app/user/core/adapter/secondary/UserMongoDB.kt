package com.spruhs.kick_app.user.core.adapter.secondary

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
    val fullName: String,
    val nickName: String,
    val email: String,
    val password: String,
    val groups: List<String>
)

@Service
class UserPersistenceAdapter(val repository: UserRepository) : UserPersistencePort {
    override fun save(user: User) {
        repository.save(
            UserDocument(
                user.id,
                user.fullName,
                user.nickName,
                user.email,
                user.password,
                user.groups
            )
        )
    }
}

@Repository
interface UserRepository : MongoRepository<UserDocument, String>