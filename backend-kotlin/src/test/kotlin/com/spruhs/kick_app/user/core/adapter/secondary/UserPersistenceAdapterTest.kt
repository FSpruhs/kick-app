package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.AbstractMongoTest
import com.spruhs.kick_app.user.core.TestUserBuilder
import com.spruhs.kick_app.user.core.domain.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

@Import(UserPersistenceAdapter::class)
class UserPersistenceAdapterTest : AbstractMongoTest() {

    @Autowired
    private lateinit var userPersistencePort: UserPersistencePort

    @Test
    fun `exists by email should be true when email exists`() {
        val user = TestUserBuilder().build()

        userPersistencePort.save(user)

        userPersistencePort.existsByEmail(user.email).let { result ->
            assertThat(result).isTrue()
        }
    }

    @Test
    fun `exists by email should be false when email does not exist`() {
        val user = TestUserBuilder().build()

        userPersistencePort.save(user)

        userPersistencePort.existsByEmail(Email("not@exists.com")).let { result ->
            assertThat(result).isFalse()
        }
    }
}