package com.spruhs.kick_app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@SpringBootApplication
@EnableMongoRepositories
class KickAppApplication

fun main(args: Array<String>) {
	runApplication<KickAppApplication>(*args)
}
