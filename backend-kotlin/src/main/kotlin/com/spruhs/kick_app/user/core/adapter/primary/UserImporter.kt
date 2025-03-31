package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.AggregateStore
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.user.core.adapter.secondary.UserDocument
import com.spruhs.kick_app.user.core.adapter.secondary.UserRepository
import com.spruhs.kick_app.user.core.application.RegisterUserCommand
import com.spruhs.kick_app.user.core.domain.Email
import com.spruhs.kick_app.user.core.domain.NickName
import com.spruhs.kick_app.user.core.domain.UserAggregate
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@Profile("dev")
class UserImporter(private val aggregateStore: AggregateStore) {

    @Value("\${app.load-default-data}")
    private var loadDefaultData: Boolean = false

    private val log = getLogger(this::class.java)

    @EventListener(ApplicationReadyEvent::class)
    suspend fun loadData() {
        if (!loadDefaultData) {
            return
        }

        defaultUsers.forEach { createTestUser(it) }

        log.info("Default user data loaded")
    }

    private suspend fun createTestUser(data: Triple<UserId, NickName, Email>) {
        val (userId, nickName, email) = data
        val user = UserAggregate(userId.value)
        user.createUser(RegisterUserCommand(nickName, email))
        aggregateStore.save(user)
    }
}

private val defaultUsers = listOf(
    Triple(UserId("user-id-1"), NickName("Spruhs"), Email("fabian@spruhs.com")),
    Triple(UserId("user-id-2"), NickName("Andi"), Email("andreas@spruhs.com")),
    Triple(UserId("user-id-3"), NickName("Casper"), Email("casper@kicken.com")),
    Triple(UserId("user-id-4"), NickName("Jannick"), Email("jannick@kicken.com")),
    Triple(UserId("user-id-5"), NickName("Enis"), Email("enis@kicken.com")),
    Triple(UserId("user-id-6"), NickName("Deniz"), Email("deniz@kicken.com")),
    Triple(UserId("user-id-7"), NickName("David"), Email("david@kicken.com")),
    Triple(UserId("user-id-8"), NickName("Junis"), Email("junis@kicken.com")),
    Triple(UserId("user-id-9"), NickName("Leon"), Email("leon@kicken.com")),
    Triple(UserId("user-id-10"), NickName("Yüksel"), Email("yüksel@kicken.com")),
    Triple(UserId("user-id-11"), NickName("Ahmet"), Email("ahmet@kicken.com")),
    Triple(UserId("user-id-12"), NickName("Amon"), Email("amon@kicken.com")),
    Triple(UserId("user-id-13"), NickName("Ben"), Email("ben@kicken.com")),
    Triple(UserId("user-id-14"), NickName("Jan"), Email("jan@kicken.com")),
    Triple(UserId("user-id-15"), NickName("Lukas"), Email("lukas@kicken.com")),
    Triple(UserId("user-id-16"), NickName("Max"), Email("max@kicken.com")),
    Triple(UserId("user-id-17"), NickName("Thorsten"), Email("thorsten@kicken.com")),
    Triple(UserId("user-id-18"), NickName("Rul"), Email("raul@kicken.com")),
    Triple(UserId("user-id-19"), NickName("phillip"), Email("phillip@kicken.com")),
    Triple(UserId("user-id-20"), NickName("Frank"), Email("Frank@kicken.com"))
)