package com.spruhs.kick_app.match.core.application

import com.spruhs.kick_app.common.es.EventPublisher
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.match.core.domain.MatchOverview
import com.spruhs.kick_app.match.core.domain.MatchOverviewPersistencePort
import org.springframework.stereotype.Service

@Service
class MatchOverviewService(
    private val matchOverviewPersistencePort: MatchOverviewPersistencePort,
    private val eventPublisher: EventPublisher,
) {
    suspend fun getMatchHistory(groupId: GroupId): MatchOverview =
        matchOverviewPersistencePort.getOverview(groupId) ?: createNewMatchOverview(groupId)

    suspend fun save(overview: MatchOverview) {
        eventPublisher.publish(overview.events)
        matchOverviewPersistencePort.save(overview)
    }

    private suspend fun createNewMatchOverview(groupId: GroupId): MatchOverview = MatchOverview(groupId)
}
