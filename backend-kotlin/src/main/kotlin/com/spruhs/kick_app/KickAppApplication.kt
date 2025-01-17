package com.spruhs.kick_app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KickAppApplication

fun main(args: Array<String>) {
	runApplication<KickAppApplication>(*args)
}
