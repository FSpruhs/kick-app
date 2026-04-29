package com.spruhs.steps

import com.mongodb.client.model.Filters
import com.spruhs.JwtTokenFactory
import io.cucumber.datatable.DataTable
import io.cucumber.java.de.Dann
import io.cucumber.java.de.Und
import io.cucumber.java.de.Wenn
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.client.postForEntity

class UserSteps(
    private val restTemplate: RestTemplate,
    private val jdbcTemplate: JdbcTemplate,
    private val mongoTemplate: MongoTemplate,
    @param:Value("\${target.base-url}") private val baseUrl: String
) {
    private lateinit var response: ResponseEntity<String>
    private var userId: String? = null
    private var newNickname: String? = null

    private fun authHeaders(userId: String) = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
        setBearerAuth(JwtTokenFactory.createToken(userId))
    }

    @Wenn("ich einen neuen Benutzer mit den folgenden Daten erstelle:")
    fun ichEinenNeuenBenutzerErstelle(dataTable: DataTable) {
        val row = dataTable.asMaps().first()
        userId = row["UserId"]!!

        val body = """
            {
                "userId": "${row["UserId"]}",
                "nickName": "${row["Nickname"]}",
                "email": "${row["E-Mail"]}"
            }
        """.trimIndent()

        response = restTemplate.postForEntity<String>(
            "$baseUrl/api/v1/user",
            HttpEntity(body, authHeaders(userId!!))
        )
    }

    @Dann("sollte die Antwort den HTTP-Statuscode {int} zurückgeben")
    fun sollteAntwortStatuscode(statusCode: Int) {
        assertThat(response.statusCode.value()).isEqualTo(statusCode)
    }

    @Und("die Antwort enthält die Id des neu erstellten Benutzers")
    fun dieAntwortEnthaeltDieId() {
        assertThat(response.body).contains(userId)
    }

    @Und("der Benutzer sollte erfolgreich in der Datenbank erstellt werden")
    fun derBenutzerSollteInDerDatenbankErsteltWerden() {
        val eventsSchema = eventsSchema()
        val pgCount = jdbcTemplate.queryForObject(
            """SELECT COUNT(*) FROM "$eventsSchema".events 
               WHERE aggregate_id = ? AND aggregate_type = 'User' AND event_type = 'USER_CREATED_V1'""",
            Int::class.java, userId
        ) ?: 0
        assertThat(pgCount).isEqualTo(1)

        val mongoCount = mongoTemplate.getCollection("users")
            .countDocuments(Filters.eq("_id", userId))
        assertThat(mongoCount).isEqualTo(1)
    }

    @Wenn("ich den Nickname des Benutzers mit der Id {string} auf {string} ändere")
    fun ichDenNicknameAendere(changeUserId: String, nickname: String) {
        newNickname = nickname

        response = restTemplate.exchange<String>(
            "$baseUrl/api/v1/user/$changeUserId/nickName?nickName=$nickname",
            HttpMethod.PUT,
            HttpEntity(null, authHeaders(changeUserId))
        )
    }

    @Und("der Benutzer sollte erfolgreich in der Datenbank aktualisiert werden")
    fun derBenutzerSollteInDerDatenbankAktualisiertWerden() {
        val eventsSchema = eventsSchema()
        val pgCount = jdbcTemplate.queryForObject(
            """SELECT COUNT(*) FROM "$eventsSchema".events 
               WHERE aggregate_id = ? AND aggregate_type = 'User' AND event_type = 'USER_NICKNAME_CHANGED_V1'""",
            Int::class.java, userId
        ) ?: 0
        assertThat(pgCount).isEqualTo(1)

        val mongoDoc = mongoTemplate.getCollection("users")
            .find(Filters.eq("_id", userId))
            .firstOrNull()
        assertThat(mongoDoc?.getString("nickName")).isEqualTo(newNickname)
    }

    private fun eventsSchema() = jdbcTemplate.queryForObject(
        "SELECT table_schema FROM information_schema.tables WHERE table_name = 'events' LIMIT 1",
        String::class.java
    ) ?: "public"
}
