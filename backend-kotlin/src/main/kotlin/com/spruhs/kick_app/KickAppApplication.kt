package com.spruhs.kick_app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableReactiveMongoRepositories
@EnableMongoRepositories
class KickAppApplication

fun main(args: Array<String>) {
	runApplication<KickAppApplication>(*args)
}
