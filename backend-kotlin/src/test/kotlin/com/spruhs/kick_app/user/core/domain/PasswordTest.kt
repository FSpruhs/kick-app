package com.spruhs.kick_app.user.core.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class PasswordTest {

    @ParameterizedTest
    @MethodSource("invalidPasswords")
    fun `fromPlaintext should throw exception if password not valid`(plaintext: String) {
        assertThatThrownBy {
            Password.fromPlaintext(plaintext)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @ParameterizedTest
    @MethodSource("validPasswords")
    fun `fromPlaintext should create password if valid`(plaintext: String) {
        assertDoesNotThrow {
            Password.fromPlaintext(plaintext)
        }
    }

    @Test
    fun `matches should return true if password matches`() {
        val plaintext = "Valid1@Password"
        val password = Password.fromPlaintext(plaintext)

        password.matches(plaintext).let { result ->
            assertThat(result).isTrue()
        }
    }

    @Test
    fun `matches should return false if password does not match`() {
        val plaintext = "Valid1@Password"
        val password = Password.fromPlaintext(plaintext)

        password.matches("Invalid@Password").let { result ->
            assertThat(result).isFalse()
        }
    }


    companion object {
        @JvmStatic
        fun invalidPasswords() = listOf(
            "Short",
            "onlylowercase",
            "ONLYUPPERCASE",
            "12345678",
            "NoNumber@Special"
        )

        @JvmStatic
        fun validPasswords() = listOf(
            "Valid1@Password",
            "AnotherValid2#Password",
            "Complex3\$Password!",
            "Strong4%Password&",
            "Secure5^Password*"
        )
    }
}