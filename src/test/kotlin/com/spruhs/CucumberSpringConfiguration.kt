package com.spruhs

import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class CucumberSpringConfiguration

