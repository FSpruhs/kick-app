package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.AbstractMongoTest
import com.spruhs.kick_app.view.core.service.StatisticProjectionRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class StatisticProjectionMongoAdapterTest : AbstractMongoTest() {

    @Autowired
    private lateinit var adapter: StatisticProjectionRepository

    @Autowired
    private lateinit var statisticsRepository: StatisticsRepository

    @Test
    fun `onSave should save statistic`() {

    }

}