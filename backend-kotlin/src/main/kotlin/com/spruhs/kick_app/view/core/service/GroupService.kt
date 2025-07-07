package com.spruhs.kick_app.view.core.service

import com.spruhs.kick_app.common.BaseEvent
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.GroupNotFoundException
import com.spruhs.kick_app.common.PlayerNotFoundException
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UnknownEventTypeException
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.group.api.GroupCreatedEvent
import com.spruhs.kick_app.group.api.GroupNameChangedEvent
import com.spruhs.kick_app.group.api.PlayerActivatedEvent
import com.spruhs.kick_app.group.api.PlayerDeactivatedEvent
import com.spruhs.kick_app.group.api.PlayerDowngradedEvent
import com.spruhs.kick_app.group.api.PlayerEnteredGroupEvent
import com.spruhs.kick_app.group.api.PlayerLeavedEvent
import com.spruhs.kick_app.group.api.PlayerPromotedEvent
import com.spruhs.kick_app.group.api.PlayerRemovedEvent
import com.spruhs.kick_app.user.api.UserImageUpdatedEvent
import com.spruhs.kick_app.user.api.UserNickNameChangedEvent
import com.spruhs.kick_app.view.api.GroupApi
import com.spruhs.kick_app.view.api.UserApi
import com.spruhs.kick_app.view.api.UserData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import kotlin.collections.plus

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
            is PlayerLeavedEvent -> handlePlayerStatusEvent(
                GroupId(event.aggregateId),
                event.userId,
                PlayerStatusType.LEAVED
            )

            is PlayerActivatedEvent -> handlePlayerStatusEvent(
                GroupId(event.aggregateId),
                event.userId,
                PlayerStatusType.ACTIVE
            )

            is PlayerDeactivatedEvent -> handlePlayerStatusEvent(
                GroupId(event.aggregateId),
                event.userId,
                PlayerStatusType.INACTIVE
            )

            is PlayerRemovedEvent -> handlePlayerStatusEvent(
                GroupId(event.aggregateId),
                event.userId,
                PlayerStatusType.REMOVED
            )

            is PlayerPromotedEvent -> handlePlayerRoleEvent(
                GroupId(event.aggregateId),
                event.userId,
                PlayerRole.COACH
            )

            is PlayerDowngradedEvent -> handlePlayerRoleEvent(
                GroupId(event.aggregateId),
                event.userId,
                PlayerRole.PLAYER
            )

            else -> throw UnknownEventTypeException(event)
        }
    }

    private fun findGroupPlayer(group: GroupProjection, userId: UserId): PlayerProjection? =
        group.players.find { it.id == userId }

    private suspend fun findGroupNameLists(userId: UserId): List<GroupNameListProjection> =
        groupNameListRepository.findByUserId(userId)

    private suspend fun handleUserNickNameChangedEvent(event: UserNickNameChangedEvent) {
        findGroupNameLists(UserId(event.aggregateId)).forEach { groupNameList ->
            val entry = groupNameList.players.find { it.userId.value == event.aggregateId }
            if (entry != null) {
                entry.name = event.nickName
                groupNameListRepository.save(groupNameList)
            }
        }
    }

    private suspend fun handleUserImageUpdatedEvent(event: UserImageUpdatedEvent) {
        findGroupNameLists(UserId(event.aggregateId)).forEach { groupNameList ->
            val entry = groupNameList.players.find { it.userId.value == event.aggregateId }
            if (entry != null) {
                entry.imageUrl = event.imageId.value
                groupNameListRepository.save(groupNameList)
            }
        }
    }

    private suspend fun handleGroupCreatedEvent(event: GroupCreatedEvent) = coroutineScope {
        val user = userApi.findUserById(event.userId)

        val groupProjection = event.toProjection(user)
        val groupNameListProjection = GroupNameListProjection(
            groupId = GroupId(event.aggregateId),
            players = mutableListOf(user.toGroupNameListEntry())
        )

        val saveGroupDeferred = async { repository.save(groupProjection) }
        val saveNameListDeferred = async { groupNameListRepository.save(groupNameListProjection) }

        saveGroupDeferred.await()
        saveNameListDeferred.await()
    }

    private suspend fun fetchGroup(groupId: GroupId): GroupProjection =
        repository.findById(groupId) ?: throw GroupNotFoundException(groupId)

    private suspend fun fetchGroupNameList(groupId: GroupId): GroupNameListProjection =
        groupNameListRepository.findByGroupId(groupId) ?: throw GroupNotFoundException(groupId)

    private suspend fun handleGroupNameChangedEvent(event: GroupNameChangedEvent) {
        fetchGroup(GroupId(event.aggregateId)).also {
            it.name = event.name
            repository.save(it)
        }
    }

    private suspend fun handlePlayerEnteredGroupEvent(event: PlayerEnteredGroupEvent) = coroutineScope {
        val userDeferred = async { userApi.findUserById(event.userId) }
        val groupDeferred = async { fetchGroup(GroupId(event.aggregateId)) }
        val groupNameListDeferred = async { fetchGroupNameList(GroupId(event.aggregateId)) }

        val user = userDeferred.await()
        val group = groupDeferred.await()
        val groupNameList = groupNameListDeferred.await()

        val updateGroupDeferred = async { updateNewPlayerInGroup(group, user, event) }
        val updateNameListDeferred = async { updateNewPlayerInGroupNameList(groupNameList, user) }

        updateGroupDeferred.await()
        updateNameListDeferred.await()
    }

    private suspend fun updateNewPlayerInGroupNameList(
        groupNameList: GroupNameListProjection,
        user: UserData,
    ) {
        val entry = groupNameList.players.find { it.userId == user.id }
        if (entry == null) {
            groupNameList.players += GroupNameListEntry(user.id, user.nickName, user.imageId?.value)
        } else {
            entry.name = user.nickName
            entry.imageUrl = user.imageId?.value
        }
        groupNameListRepository.save(groupNameList)
    }

    private suspend fun updateNewPlayerInGroup(
        group: GroupProjection,
        user: UserData,
        event: PlayerEnteredGroupEvent
    ) {
        val player = findGroupPlayer(group, event.userId)
        if (player == null) {
            group.players += PlayerProjection(
                id = event.userId,
                status = event.userStatus,
                role = event.userRole,
                email = user.email,
            )
        } else {
            with(player) {
                status = event.userStatus
                role = event.userRole
                email = user.email
            }
        }
        repository.save(group)
    }

    private suspend fun handlePlayerRoleEvent(groupId: GroupId, userId: UserId, role: PlayerRole) {
        val group = fetchGroup(groupId)
        findGroupPlayer(group, userId)?.also {
            it.role = role
            repository.save(group)
        }
    }

    private suspend fun handlePlayerStatusEvent(groupId: GroupId, userId: UserId, status: PlayerStatusType) {
        val group = fetchGroup(groupId)
        findGroupPlayer(group, userId)?.also {
            it.status = status
            repository.save(group)
        }
    }

    suspend fun getGroup(groupId: GroupId, userId: UserId): GroupProjection {
        val group = fetchGroup(groupId)
        findGroupPlayer(group, userId)?.also {
            require(it.hasMemberStatus()) { throw UserNotAuthorizedException(userId) }
        } ?: throw PlayerNotFoundException(userId)

        return group
    }

    suspend fun getPlayer(groupId: GroupId, userId: UserId, requesterId: UserId): PlayerProjection {
        val group = fetchGroup(groupId)
        findGroupPlayer(group, requesterId)?.also {
            require(it.hasMemberStatus()) { throw UserNotAuthorizedException(requesterId) }
        } ?: throw PlayerNotFoundException(requesterId)

        return findGroupPlayer(group, userId)
            ?: throw PlayerNotFoundException(userId)
    }

    suspend fun getGroupNameList(groupId: GroupId, requesterId: UserId): List<GroupNameListEntry> {
        val groupNameList = fetchGroupNameList(groupId)
        groupNameList.players.find { it.userId == requesterId }
            ?: throw UserNotAuthorizedException(requesterId)
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

    fun getActivePlayers(): List<UserId> =
        players.filter { it.status == PlayerStatusType.ACTIVE }
            .map { it.id }
            .distinct()
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
) : GroupApi {
    override suspend fun isActiveMember(
        groupId: GroupId,
        userId: UserId
    ): Boolean = repository.findById(groupId)?.isActivePlayer(userId) ?: false

    override suspend fun isActiveCoach(
        groupId: GroupId,
        userId: UserId
    ): Boolean = repository.findById(groupId)?.isActiveCoach(userId) ?: false

    override suspend fun getActivePlayers(groupId: GroupId): List<UserId> =
        repository.findById(groupId)?.getActivePlayers() ?: emptyList()
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

private fun GroupCreatedEvent.toProjection(user: UserData): GroupProjection = GroupProjection(
    id = GroupId(aggregateId),
    name = name,
    players = listOf(
        PlayerProjection(
            id = userId,
            status = userStatus,
            role = userRole,
            email = user.email,
        )
    ),
)

private fun UserData.toGroupNameListEntry(): GroupNameListEntry = GroupNameListEntry(
    userId = id,
    name = nickName,
)

private fun PlayerProjection.hasMemberStatus(): Boolean =
    this.status in listOf(PlayerStatusType.ACTIVE, PlayerStatusType.INACTIVE)
