package com.spruhs.kick_app.match.core.adapter.secondary

import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.match.core.domain.MatchNumber
import com.spruhs.kick_app.match.core.domain.PlayerOverview
import com.spruhs.kick_app.match.core.domain.PlayerOverviewEntry
import com.spruhs.kick_app.match.core.domain.PlayerOverviewPersistencePort
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class PlayerOverviewPersistenceAdapter(
    private val playerOverviewRepository: PlayerOverviewRepository,
) : PlayerOverviewPersistencePort {
    override suspend fun getOverview(groupId: GroupId): PlayerOverview? =
        playerOverviewRepository.findByGroupId(groupId.value).awaitFirstOrNull()?.toDomain()

    override suspend fun save(overview: PlayerOverview) {
        playerOverviewRepository.save(overview.toDocument()).awaitSingle()
    }
}

@Repository
interface PlayerOverviewRepository : ReactiveMongoRepository<PlayerOverviewDocument, String> {
    fun findByGroupId(groupId: String): Mono<PlayerOverviewDocument>
}

@Document(collection = "player_overviews")
data class PlayerOverviewDocument(
    @Id
    val groupId: String,
    val entries: List<PlayerOverviewEntryDocument>,
)

data class PlayerOverviewEntryDocument(
    val userId: String,
    val attendancePoints: Int,
    val lastWaitingBenchMatchNumber: Int?,
)

private fun PlayerOverview.toDocument() =
    PlayerOverviewDocument(
        groupId = groupId.value,
        entries = entries.map { it.toDocument() },
    )

private fun PlayerOverviewEntry.toDocument() =
    PlayerOverviewEntryDocument(
        userId = userId.value,
        attendancePoints = attendancePoints,
        lastWaitingBenchMatchNumber = lastWaitingBenchMatchNumber?.value,
    )

private fun PlayerOverviewDocument.toDomain() =
    PlayerOverview(
        groupId = GroupId(groupId),
        entries = entries.map { it.toDomain() }.toMutableList(),
    )

private fun PlayerOverviewEntryDocument.toDomain() =
    PlayerOverviewEntry(
        userId = UserId(userId),
        attendancePoints = attendancePoints,
        lastWaitingBenchMatchNumber = lastWaitingBenchMatchNumber?.let { MatchNumber(it) },
    )
