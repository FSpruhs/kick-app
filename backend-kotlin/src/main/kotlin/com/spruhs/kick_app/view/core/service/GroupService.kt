package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UnknownEventTypeException
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.api.GroupCreatedEvent
import com.spruhs.kick_app.group.api.GroupNameChangedEvent
import com.spruhs.kick_app.group.api.PlayerActivatedEvent
import com.spruhs.kick_app.group.api.PlayerDeactivatedEvent
import com.spruhs.kick_app.group.api.PlayerDowngradedEvent
import com.spruhs.kick_app.group.api.PlayerEnteredGroupEvent
import com.spruhs.kick_app.group.api.PlayerLeavedEvent
import com.spruhs.kick_app.group.api.PlayerPromotedEvent
import com.spruhs.kick_app.group.api.PlayerRemovedEvent
import com.spruhs.kick_app.group.core.domain.GroupNotFoundException
import com.spruhs.kick_app.user.api.UserImageUpdatedEvent
import com.spruhs.kick_app.user.api.UserNickNameChangedEvent
import com.spruhs.kick_app.view.api.GroupApi
import com.spruhs.kick_app.view.api.UserApi
import org.springframework.stereotype.Service

@Service
class GroupService(
    private val repository: GroupProjectionRepository,
    private val groupNameListRepository: GroupNameListProjectionRepository,
    private val userApi: UserApi
) {
    suspend fun whenEvent(event: BaseEvent) {
        when (event) {
            is UserNickNameChangedEvent -> handleUserNickNameChangedEvent(event)
            is UserImageUpdatedEvent -> handleUserImageUpdatedEvent(event)

            is GroupCreatedEvent -> handleGroupCreatedEvent(event)
            is GroupNameChangedEvent -> handleGroupNameChangedEvent(event)
            is PlayerEnteredGroupEvent -> handlePlayerEnteredGroupEvent(event)
            is PlayerLeavedEvent -> handlePlayerStatusEvent(GroupId(event.aggregateId), event.userId, PlayerStatusType.LEAVED)
            is PlayerActivatedEvent -> handlePlayerStatusEvent(GroupId(event.aggregateId), event.userId, PlayerStatusType.ACTIVE)
            is PlayerDeactivatedEvent -> handlePlayerStatusEvent(GroupId(event.aggregateId), event.userId, PlayerStatusType.INACTIVE)
            is PlayerRemovedEvent -> handlePlayerStatusEvent(GroupId(event.aggregateId), event.userId, PlayerStatusType.REMOVED)
            is PlayerPromotedEvent -> handlePlayerRoleEvent(GroupId(event.aggregateId), event.userId, PlayerRole.COACH)
            is PlayerDowngradedEvent -> handlePlayerRoleEvent(GroupId(event.aggregateId), event.userId, PlayerRole.PLAYER)
            else -> throw UnknownEventTypeException(event)
        }
    }

    private suspend fun handleUserNickNameChangedEvent(event: UserNickNameChangedEvent) {
        val groupNameLists = groupNameListRepository.findByUserId(UserId(event.aggregateId))
        for (groupNameList in groupNameLists) {
            val entry = groupNameList.players.find { it.userId.value == event.aggregateId }
            if (entry != null) {
                entry.name = event.nickName
                groupNameListRepository.save(groupNameList)
            }
        }
    }

    private suspend fun handleUserImageUpdatedEvent(event: UserImageUpdatedEvent) {
        val groupNameLists = groupNameListRepository.findByUserId(UserId(event.aggregateId))
        for (groupNameList in groupNameLists) {
            val entry = groupNameList.players.find { it.userId.value == event.aggregateId }
            if (entry != null) {
                entry.imageUrl = event.imageId.value
                groupNameListRepository.save(groupNameList)
            }
        }
    }

    private suspend fun handleGroupCreatedEvent(event: GroupCreatedEvent) {
        val user = userApi.findUserById(event.userId)
        GroupProjection(
            id = GroupId(event.aggregateId),
            name = event.name,
            players = listOf(PlayerProjection(
                id = event.userId,
                status = event.userStatus,
                role = event.userRole,
                email = user.email,
            )),
        ).also {
            repository.save(it)
        }

        GroupNameListProjection(
            groupId = GroupId(event.aggregateId),
            players = mutableListOf(GroupNameListEntry(user.id, user.nickName))
        ).also {
            groupNameListRepository.save(it)
        }
    }

    private suspend fun fetchGroup(groupId: GroupId): GroupProjection =
        repository.findById(groupId) ?: throw GroupNotFoundException(groupId)

    private suspend fun fetchGroupNameList(groupId: GroupId): GroupNameListProjection =
        groupNameListRepository.findByGroupId(groupId) ?: throw GroupNotFoundException(groupId)

    private suspend fun handleGroupNameChangedEvent(event: GroupNameChangedEvent) {
        fetchGroup(GroupId(event.aggregateId)).let {
            it.name = event.name
            repository.save(it)
        }
    }

    private suspend fun handlePlayerEnteredGroupEvent(event: PlayerEnteredGroupEvent) {
        val user = userApi.findUserById(event.userId)
        val group = fetchGroup(GroupId(event.aggregateId))
        val player = group.players.find { it.id == event.userId }
        if (player == null) {
            group.players += PlayerProjection(
                id = event.userId,
                status = event.userStatus,
                role = event.userRole,
                email = user.email,
            )
        } else {
            player.status = event.userStatus
            player.role = event.userRole
            player.email = user.email
        }
        repository.save(group)

        val groupNameList = fetchGroupNameList(GroupId(event.aggregateId))
        groupNameList.players += GroupNameListEntry(user.id, user.nickName)
        groupNameListRepository.save(groupNameList)
    }

    private suspend fun handlePlayerRoleEvent(groupId: GroupId, userId: UserId, role: PlayerRole) {
        val group = fetchGroup(groupId)
        val player = group.players.find { it.id == userId }
        if (player != null) {
            player.role = role
            repository.save(group)
        }
    }

    private suspend fun handlePlayerStatusEvent(groupId: GroupId, userId: UserId, status: PlayerStatusType) {
        val group = fetchGroup(groupId)
        val player = group.players.find { it.id == userId }
        if (player != null) {
            player.status = status
            repository.save(group)
        }
    }

    suspend fun getGroup(groupId: GroupId, userId: UserId): GroupProjection {
        val group = fetchGroup(groupId)
        val player = group.players.find { it.id == userId }
            ?: throw IllegalArgumentException("User not found in group")
        require(player.status == PlayerStatusType.ACTIVE || player.status == PlayerStatusType.INACTIVE) {
            "User is not an active or inactive member of the group"
        }
        return group
    }

    suspend fun getPlayer(groupId: GroupId, userId: UserId, requesterId: UserId): PlayerProjection {
        val group = fetchGroup(groupId)
        val player = group.players.find { it.id == userId }
            ?: throw IllegalArgumentException("User not found in group")
        val requester = group.players.find { it.id == requesterId }
            ?: throw IllegalArgumentException("Requester not found in group")
        require(requester.status == PlayerStatusType.ACTIVE || requester.status == PlayerStatusType.INACTIVE) {
            "User is not an active or inactive member of the group"
        }
        return player
    }

    suspend fun getGroupNameList(groupId: GroupId, requesterId: UserId): List<GroupNameListEntry> {
        val groupNameList = fetchGroupNameList(groupId)
        val requester = groupNameList.players.find { it.userId == requesterId }
            ?: throw IllegalArgumentException("Requester not found in group name list")

        require(requester.userId == requesterId) {
            "Requester is not authorized to view the group name list"
        }
        return groupNameList.players
    }
}

data class GroupNameListProjection(
    val groupId: GroupId,
    var players: List<GroupNameListEntry>,
)

data class GroupNameListEntry(
    val userId: UserId,
    var name: String,
    var imageUrl: String? = null,
)

data class GroupProjection(
    val id: GroupId,
    var name: String,
    var players: List<PlayerProjection>,
) {
    fun isActivePlayer(userId: UserId): Boolean =
        players.any { it.id == userId && it.status == PlayerStatusType.ACTIVE }

    fun isActiveCoach(userId: UserId): Boolean =
        players.any { it.id == userId && it.role == PlayerRole.COACH && it.status == PlayerStatusType.ACTIVE }
}

data class PlayerProjection(
    val id: UserId,
    var status: PlayerStatusType,
    var role: PlayerRole,
    var email: String,
)

@Service
class GroupApiService(
    private val repository: GroupProjectionRepository,
    private val groupNameListRepository: GroupNameListProjectionRepository
) : GroupApi {
    override suspend fun isActiveMember(
        groupId: GroupId,
        userId: UserId
    ): Boolean {
        val group = repository.findById(groupId) ?: return false
        return group.isActivePlayer(userId)
    }

    override suspend fun isActiveCoach(
        groupId: GroupId,
        userId: UserId
    ): Boolean {
        val group = repository.findById(groupId) ?: return false
        return group.isActiveCoach(userId)
    }

    override suspend fun getActivePlayers(groupId: GroupId): List<UserId> {
        val group = repository.findById(groupId) ?: return emptyList()
        return group.players.filter { it.status == PlayerStatusType.ACTIVE }
            .map { it.id }
    }

    override suspend fun getGroupNameList(groupId: GroupId): Map<UserId, String> {
        val groupNameList = groupNameListRepository.findByGroupId(groupId)
            ?: return emptyMap()
        return groupNameList.players.associate { it.userId to it.name }
    }

    override suspend fun getUserGroups(userId: UserId): List<GroupId> {
        return groupNameListRepository.findByUserId(userId).map { it.groupId }
    }

}

interface GroupProjectionRepository {
    suspend fun findById(groupId: GroupId): GroupProjection?
    suspend fun save(groupProjection: GroupProjection)
}

interface GroupNameListProjectionRepository {
    suspend fun findByGroupId(groupId: GroupId): GroupNameListProjection?
    suspend fun findByUserId(userId: UserId): List<GroupNameListProjection>
    suspend fun save(groupNameList: GroupNameListProjection)
}