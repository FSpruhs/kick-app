package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.helper.getLogger
import com.spruhs.kick_app.user.core.domain.*
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("oauth2")
@Service
class KeycloakAdapter(val keycloak: Keycloak) : UserIdentityProviderPort {

    private val log = getLogger(this::class.java)

    @Value("\${keycloak.realm}")
    private var realm: String? = null

    override suspend fun save(email: Email, nickName: NickName, password: Password?, userId: UserId?): UserId {
        val response = UserRepresentation().apply {
            this.username = nickName.value
            this.firstName = nickName.value + " firstname"
            this.lastName = nickName.value + " lastname"
            this.email = email.value
            this.isEnabled = true
            this.isEmailVerified = true
            this.requiredActions = listOf(UPDATE_PASSWORD_COMMAND)
        }.let {
            keycloak.realm(realm).users().create(it)
        }

        return if (response.status == 201) {
            log.info("Keycloack user ${nickName.value} successfully created.")
            val userId = (response.metadata["Location"]?.get(0) as String).substringAfterLast("/")
            keycloak.realm(realm).users().get(userId).executeActionsEmail(listOf(UPDATE_PASSWORD_COMMAND))
            UserId(userId)
        } else {
            val errorMessage = response.readEntity(String::class.java)
            log.error("Fehler: $errorMessage")
            throw CreateUserIdentityProviderException(errorMessage)
        }
    }

    override suspend fun changeNickName(userId: UserId, nickName: NickName) {
        val userResource = keycloak.realm(realm).users().get(userId.value)
        val userRepresentation: UserRepresentation = userResource.toRepresentation()
        userRepresentation.username = nickName.value
        userResource.update(userRepresentation)
    }

    companion object {
        private const val UPDATE_PASSWORD_COMMAND = "UPDATE_PASSWORD"
    }
}