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
        id = "da082e6e-b4c1-40a4-8144-9098a2d819d9",
        firstName = "Fabian",
        lastName = "Spruhs",
        nickName = "Spruhs",
        email = "fabian@spruhs.com",
        groups = listOf("donnerstags-kick", "sonntags-kick")
    ),
    UserDocument(
        id = "user-id-2",
        firstName = "Andreas",
        lastName = "Spruhs",
        nickName = "Andi",
        email = "andreas@spruhs.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-3",
        firstName = "Casper",
        lastName = "Casper",
        nickName = "Casper",
        email = "casper@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-4",
        firstName = "Jannick",
        lastName = "Jannick",
        nickName = "Jannick",
        email = "jannick@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-5",
        firstName = "Enis",
        lastName = "Enis",
        nickName = "Enis",
        email = "enis@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-6",
        firstName = "Deniz",
        lastName = "Deniz",
        nickName = "Deniz",
        email = "deniz@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-7",
        firstName = "David",
        lastName = "David",
        nickName = "David",
        email = "david@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-8",
        firstName = "Junis",
        lastName = "Junis",
        nickName = "Junis",
        email = "junis@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-9",
        firstName = "Leon",
        lastName = "Leon",
        nickName = "Leon",
        email = "leon@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-10",
        firstName = "Yüksel",
        lastName = "Yüksel",
        nickName = "Yüksel",
        email = "yüksel@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-11",
        firstName = "Ahmet",
        lastName = "Ahmet",
        nickName = "Ahmet",
        email = "ahmet@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-12",
        firstName = "Amon",
        lastName = "Amon",
        nickName = "Amon",
        email = "amon@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-13",
        firstName = "Ben",
        lastName = "Ben",
        nickName = "Ben",
        email = "ben@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-14",
        firstName = "Jan",
        lastName = "Jan",
        nickName = "Jan",
        email = "jan@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-15",
        firstName = "Lukas",
        lastName = "Lukas",
        nickName = "Lukas",
        email = "lukas@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-16",
        firstName = "Max",
        lastName = "Max",
        nickName = "Max",
        email = "max@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-17",
        firstName = "Thorsten",
        lastName = "Thorsten",
        nickName = "Thorsten",
        email = "thorsten@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-18",
        firstName = "Raul",
        lastName = "Raul",
        nickName = "Rul",
        email = "raul@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-19",
        firstName = "Phillip",
        lastName = "Phillip",
        nickName = "phillip",
        email = "phillip@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-20",
        firstName = "Frank",
        lastName = "Frank",
        nickName = "Frank",
        email = "Frank@kicken.com",
        groups = listOf("donnerstags-kick")
    ),



)