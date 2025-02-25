package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.user.core.domain.User
import com.spruhs.kick_app.user.core.domain.UserIdentityProviderPort
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service

@Service
class KeycloakAdapter(val keycloak: Keycloak) : UserIdentityProviderPort {

    private val log = getLogger(this::class.java)

    override fun save(user: User) {
        val keycloakUser = UserRepresentation()
        keycloakUser.id = user.id.value
        keycloakUser.username = user.nickName.value
        keycloakUser.email = user.email.value
        keycloakUser.firstName = user.fullName.firstName.value
        keycloakUser.lastName = user.fullName.lastName.value
        keycloakUser.isEnabled = true
        keycloakUser.isEmailVerified = true

        val response = keycloak.realm("kick-app").users().create(keycloakUser)

        println("Response Status: ${response.status}")
        if (response.status != 201) {
            val errorMessage = response.readEntity(String::class.java)
            log.info("Fehler: $errorMessage")
        } else {
            log.info("Benutzer erfolgreich erstellt.")
        }
    }
}