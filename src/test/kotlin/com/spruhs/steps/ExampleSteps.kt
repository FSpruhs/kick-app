package com.spruhs.steps

import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

class ExampleSteps(
    private val restTemplate: RestTemplate,
    @param:Value("\${target.base-url}") private val baseUrl: String
) {

    private lateinit var response: ResponseEntity<String>

    @Given("der Service ist erreichbar")
    fun derServiceIstErreichbar() {
        // Vorbedingung: Annahme, dass der Service unter $baseUrl gestartet ist
    }

    @When("ich den Health-Endpunkt aufrufe")
    fun ichDenHealthEndpunktAufrufe() {
        response = restTemplate.getForEntity<String>("$baseUrl/actuator/health")
    }

    @Then("erhalte ich den HTTP-Statuscode {int}")
    fun ichErhalteDenStatuscode(statusCode: Int) {
        assertThat(response.statusCode.value()).isEqualTo(statusCode)
    }

    @And("die Antwort enthält den Status {string}")
    fun dieAntwortEnthaeltDenStatus(status: String) {
        assertThat(response.body).contains(status)
    }
}
