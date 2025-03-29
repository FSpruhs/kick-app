package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.user.core.domain.*
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class KeycloakAdapter(val keycloak: Keycloak) : UserIdentityProviderPort {

    private val log = getLogger(this::class.java)

    @Value("\${keycloak.realm}")
    private var realm: String? = null

    override fun save(email: Email, nickName: NickName): UserId {
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

    override fun changeNickName(userId: UserId, nickName: NickName) {
        val userResource = keycloak.realm(realm).users().get(userId.value)
        val userRepresentation: UserRepresentation = userResource.toRepresentation()
        userRepresentation.username = nickName.value
        userResource.update(userRepresentation)
    }

    companion object {
        private const val UPDATE_PASSWORD_COMMAND = "UPDATE_PASSWORD"
    }
}