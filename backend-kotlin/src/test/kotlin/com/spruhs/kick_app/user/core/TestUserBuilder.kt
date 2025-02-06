package com.spruhs.kick_app.user.core

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.core.application.RegisterUserCommand
import com.spruhs.kick_app.user.core.domain.*

class TestUserBuilder {
    private var id: String = "Test id"
    private var firstName: String = "test first name"
    private var lastName: String = "test last name"
    private var nickName: String = "test nick name"
    private var email: String = "test@testen.com"
    private var password: String = "test password"
    private var groups: List<String> = listOf("test group")

    fun withId(id: String) = apply { this.id = id }
    fun withFirstName(firstName: String) = apply { this.firstName = firstName }
    fun withLastName(lastName: String) = apply { this.lastName = lastName }
    fun withNickName(nickName: String) = apply { this.nickName = nickName }
    fun withEmail(email: String) = apply { this.email = email }
    fun withPassword(password: String) = apply { this.password = password }
    fun withGroups(groups: List<String>) = apply { this.groups = groups }

    fun buildRegisterUserCommand(): RegisterUserCommand {
        return RegisterUserCommand(
            FirstName(firstName),
            LastName(lastName),
            NickName(nickName),
            Email(email),
        )
    }

    fun build(): User {
        return User(
            UserId(id),
            FullName(FirstName(firstName), LastName(lastName)),
            NickName(nickName),
            Email(email),
            groups.map { GroupId(it) }
        )
    }
}