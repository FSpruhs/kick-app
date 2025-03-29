package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.user.core.adapter.secondary.UserDocument
import com.spruhs.kick_app.user.core.adapter.secondary.UserRepository
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
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
        nickName = "Spruhs",
        email = "fabian@spruhs.com",
        groups = listOf("donnerstags-kick", "sonntags-kick")
    ),
    UserDocument(
        id = "user-id-2",
        nickName = "Andi",
        email = "andreas@spruhs.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-3",
        nickName = "Casper",
        email = "casper@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-4",
        nickName = "Jannick",
        email = "jannick@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-5",
        nickName = "Enis",
        email = "enis@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-6",
        nickName = "Deniz",
        email = "deniz@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-7",
        nickName = "David",
        email = "david@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-8",
        nickName = "Junis",
        email = "junis@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-9",
        nickName = "Leon",
        email = "leon@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-10",
        nickName = "Yüksel",
        email = "yüksel@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-11",
        nickName = "Ahmet",
        email = "ahmet@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-12",
        nickName = "Amon",
        email = "amon@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-13",
        nickName = "Ben",
        email = "ben@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-14",
        nickName = "Jan",
        email = "jan@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-15",
        nickName = "Lukas",
        email = "lukas@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-16",
        nickName = "Max",
        email = "max@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-17",
        nickName = "Thorsten",
        email = "thorsten@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-18",
        nickName = "Rul",
        email = "raul@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-19",
        nickName = "phillip",
        email = "phillip@kicken.com",
        groups = listOf("donnerstags-kick")
    ),
    UserDocument(
        id = "user-id-20",
        nickName = "Frank",
        email = "Frank@kicken.com",
        groups = listOf("donnerstags-kick")
    ),



)