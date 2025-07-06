package com.spruhs.kick_app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableMongoRepositories
@EnableWebFluxSecurity
class KickAppApplication

fun main(args: Array<String>) {
	runApplication<KickAppApplication>(*args)
}


