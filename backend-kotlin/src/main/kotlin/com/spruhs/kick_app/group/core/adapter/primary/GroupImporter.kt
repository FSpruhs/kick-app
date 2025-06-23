package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.getLogger
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev")
class GroupImporter() {

    @Value("\${app.load-default-data}")
    private var loadDefaultData: Boolean = false

    private val log = getLogger(this::class.java)

    @PostConstruct
    fun loadData() {
        if (!loadDefaultData) {
            return
        }

        log.info("Default group data loaded")

    }
}