package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.domain.Name
import com.spruhs.kick_app.view.api.UserApi
import org.springframework.stereotype.Service

@Service
class GroupService(
    private val repository: GroupProjectionRepository,
    private val userApi: UserApi
) {
    suspend fun getGroup(groupId: GroupId, userId: UserId): GroupProjection {
        val group = repository.findById(groupId)
        val player = group.players.find { it.id == userId }
            ?: throw IllegalArgumentException("User not found in group")
        require(player.status == PlayerStatusType.ACTIVE || player.status == PlayerStatusType.INACTIVE) {
            "User is not an active or inactive member of the group"
        }
        return group
    }
}

data class GroupProjection(
    val id: GroupId,
    val name: Name,
    val players: List<PlayerProjection>,
) {
    fun isActivePlayer(userId: UserId): Boolean =
        players.any { it.id == userId && it.status == PlayerStatusType.ACTIVE }

    fun isActiveCoach(userId: UserId): Boolean =
        players.any { it.id == userId && it.role == PlayerRole.COACH && it.status == PlayerStatusType.ACTIVE }

    fun isPlayer(userId: UserId): Boolean =
        players.any { it.id == userId && (it.status == PlayerStatusType.ACTIVE || it.status == PlayerStatusType.INACTIVE) }
}

data class PlayerProjection(
    val id: UserId,
    val status: PlayerStatusType,
    val role: PlayerRole,
    val avatarUrl: String? = null,
    val email: String,
)

interface GroupProjectionRepository {
    suspend fun findById(groupId: GroupId): GroupProjection?
    suspend fun save(groupProjection: GroupProjection)
}