package com.spruhs.kick_app.user.core.domain

import com.mongodb.assertions.Assertions.doesNotThrow
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class EmailTest {
    @ParameterizedTest
    @ValueSource(strings = [
        "",
        "plainaddress",
        "@missingusername.com",
        "username@.com",
        "username@.com.",
        "username@domain..com",
        "username@domain,com",
        "username@domain@domain.com",
        "username@domain..com",
    ])
    fun `should throw exception for invalid email`(invalidEmail: String) {
        assertThatThrownBy { Email(invalidEmail) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "user@example.com",
        "user.name+tag+sorting@example.com",
        "user.name@example.co.uk",
        "user_name@example.com",
        "user-name@example.com"
    ])
    fun `should not throw exception for valid email`(validEmail: String) {
        doesNotThrow {
            Email(validEmail)
        }
    }
}