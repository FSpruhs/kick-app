package com.spruhs.kick_app.user.core.domain

import com.mongodb.assertions.Assertions.doesNotThrow
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class NickNameTest {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "a",
            "abcdefghijklmnopqrstu",
        ]
    )
    fun `should throw exception for valid nick name`(invalidNickName: String) {
        assertThatThrownBy {
            NickName(invalidNickName)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "ab",
            "abcdefghijklmnopqrst",
        ]
    )
    fun `should not throw exception for valid nick name`(validNickName: String) {
        doesNotThrow {
            NickName(validNickName)
        }
    }
}