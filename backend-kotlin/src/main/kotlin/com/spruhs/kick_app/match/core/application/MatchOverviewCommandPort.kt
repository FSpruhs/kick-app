package com.spruhs.kick_app.match.core.application

import com.spruhs.kick_app.common.es.BaseEvent
import com.spruhs.kick_app.common.es.UnknownEventTypeException
import com.spruhs.kick_app.common.helper.KeyedMutex
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.match.api.MatchCanceledEvent
import com.spruhs.kick_app.match.api.MatchResultEnteredEvent
import com.spruhs.kick_app.match.core.domain.MatchOverview
import org.springframework.stereotype.Service

@Service
class MatchOverviewCommandPort(
    private val matchOverviewService: MatchOverviewService,
    private val mutex: KeyedMutex<GroupId> = KeyedMutex(),
) {
    suspend fun onEvent(event: BaseEvent) {
        when (event) {
            is MatchCanceledEvent -> onMatchCanceled(event)
            is MatchResultEnteredEvent -> onMatchResultEntered(event)
            else -> throw UnknownEventTypeException(event)
        }
    }

    private suspend fun onMatchCanceled(event: MatchCanceledEvent) {
        handle(event.groupId) { matchOverview ->
            matchOverview.cancel(MatchId(event.aggregateId))
        }
    }

    private suspend fun onMatchResultEntered(event: MatchResultEnteredEvent) {
        handle(event.groupId) { matchOverview ->
            matchOverview.resultEntered(MatchId(event.aggregateId))
        }
    }

    private suspend inline fun handle(
        groupId: GroupId,
        crossinline block: (MatchOverview) -> Unit,
    ) = mutex.withKeyLock(groupId) {
        matchOverviewService.getMatchHistory(groupId).also {
            block(it)
            matchOverviewService.save(it)
        }
    }
}
