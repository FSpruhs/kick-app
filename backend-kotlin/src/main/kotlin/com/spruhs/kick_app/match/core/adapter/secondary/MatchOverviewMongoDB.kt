package com.spruhs.kick_app.match.core.adapter.secondary

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.MatchId
import com.spruhs.kick_app.match.core.domain.MatchOverview
import com.spruhs.kick_app.match.core.domain.MatchOverviewEntry
import com.spruhs.kick_app.match.core.domain.MatchOverviewPersistencePort
import com.spruhs.kick_app.match.core.domain.MatchState
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class MatchOverviewPersistenceAdapter(
    private val matchOverviewRepository: MatchOverviewRepository,
) : MatchOverviewPersistencePort {
    override suspend fun getOverview(groupId: GroupId): MatchOverview? =
        matchOverviewRepository.findByGroupId(groupId.value).awaitFirstOrNull()?.toDomain()

    override suspend fun save(overview: MatchOverview) {
        matchOverviewRepository.save(overview.toDocument()).awaitSingle()
    }
}

@Repository
interface MatchOverviewRepository : ReactiveMongoRepository<MatchOverviewDocument, String> {
    fun findByGroupId(groupId: String): Mono<MatchOverviewDocument>
}

@Document(collection = "match_overviews")
data class MatchOverviewDocument(
    @Id val id: String,
    val groupId: String,
    val entries: List<MatchOverviewEntryDocument>,
)

data class MatchOverviewEntryDocument(
    val matchId: String,
    val matchNumber: Int,
    val start: LocalDateTime,
    val state: String,
)

private fun MatchOverview.toDocument() =
    MatchOverviewDocument(
        id = groupId.value,
        groupId = groupId.value,
        entries = entries.map { it.toDocument() },
    )

private fun MatchOverviewEntry.toDocument() =
    MatchOverviewEntryDocument(
        matchId = matchId.value,
        matchNumber = matchNumber,
        start = start,
        state = state.name,
    )

private fun MatchOverviewDocument.toDomain() =
    MatchOverview(
        groupId = GroupId(groupId),
        entries = entries.map { it.toDomain() }.toMutableList(),
    )

private fun MatchOverviewEntryDocument.toDomain() =
    MatchOverviewEntry(
        matchId = MatchId(matchId),
        matchNumber = matchNumber,
        start = start,
        state = MatchState.valueOf(state),
    )
