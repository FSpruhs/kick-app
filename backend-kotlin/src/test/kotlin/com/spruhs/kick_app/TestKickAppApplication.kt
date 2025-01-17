package com.spruhs.kick_app

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<KickAppApplication>().with(TestcontainersConfiguration::class).run(*args)
}
