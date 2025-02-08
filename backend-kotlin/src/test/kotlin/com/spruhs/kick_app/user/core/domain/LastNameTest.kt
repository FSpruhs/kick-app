package com.spruhs.kick_app.user.core.domain

import com.mongodb.assertions.Assertions.doesNotThrow
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class LastNameTest {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "a",
            "abcdefghijklmnopqrstu",
        ]
    )
    fun `should throw exception for valid last name`(invalidLastName: String) {
        assertThatThrownBy {
            LastName(invalidLastName)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "ab",
            "abcdefghijklmnopqrst",
        ]
    )
    fun `should not throw exception for valid last name`(validLastName: String) {
        doesNotThrow {
            LastName(validLastName)
        }
    }
}