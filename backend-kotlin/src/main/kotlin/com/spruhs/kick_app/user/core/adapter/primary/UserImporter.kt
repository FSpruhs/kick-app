package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.user.core.adapter.secondary.UserDocument
import com.spruhs.kick_app.user.core.adapter.secondary.UserRepository
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev")
class UserImporter(private val userRepository: UserRepository) {

    @Value("\${app.load-default-data}")
    private var loadDefaultData: Boolean = false

    private val log = getLogger(this::class.java)

    @PostConstruct
    fun loadData() {
        if (!loadDefaultData) {
            return
        }

        userRepository.deleteAll()
        userRepository.saveAll(defaultUsers)

        log.info("Default user data loaded")
    }
}

private val defaultUsers = listOf(
    UserDocument(
        id = "4c95a419-521f-423e-a787-21f48e07cd7e",
        firstName = "Fabian",
        lastName = "Spruhs",
        nickName = "Spruhs",
        email = "fabian@spruhs.com",
        groups = listOf("1")
    ),
    UserDocument(
        id = "2",
        firstName = "Andreas",
        lastName = "Spruhs",
        nickName = "Andi",
        email = "andreas@spruhs.com",
        groups = listOf("1")
    ),
)