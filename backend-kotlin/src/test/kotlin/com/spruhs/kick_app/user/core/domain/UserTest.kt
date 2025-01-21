package com.spruhs.kick_app.user.core.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UserTest {
    @Test
    fun `should create a user`() {
        val firstName = FirstName("John")
        val lastName = LastName("Doe")
        val fullName = FullName(firstName, lastName)
        val nickName = NickName("john_doe")
        val email = Email("john@doe.com")
        val password = Password("password")
        val user = createUser(
            fullName = fullName,
            nickName = nickName,
            email = email,
            password = password
        )

        assertThat(user.fullName).isEqualTo(fullName)
        assertThat(user.nickName).isEqualTo(nickName)
        assertThat(user.email).isEqualTo(email)
        assertThat(user.password).isEqualTo(password)
        assertThat(user.groups).isEmpty()
        assertThat(user.id).isNotNull()
        assertThat(user.id.value).isNotEmpty()
    }
}