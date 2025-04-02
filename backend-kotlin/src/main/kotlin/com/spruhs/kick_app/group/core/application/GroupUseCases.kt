package com.spruhs.kick_app.group.core.application

import com.spruhs.kick_app.common.*
import com.spruhs.kick_app.group.core.domain.*
import com.spruhs.kick_app.user.api.UserData
import kotlinx.coroutines.*
import org.springframework.stereotype.Service

private fun Group.toGroupDetails(users: Map<UserId, UserData>): GroupDetail = GroupDetail(
    id = id.value,
    name = name.value,
    players = players.map { player ->
        PlayerDetail(
            id = player.id.value,
            nickName = users.getValue(player.id).nickName,
            role = player.role,
            status = player.status.type(),
        )
    },
    invitedUsers = invitedUsers.map { it.value },
)

data class UpdatePlayerCommand(
    val userId: UserId,
    val updatingUserId: UserId,
    val groupId: GroupId,
    val newStatus: PlayerStatusType? = null,
    val newRole: PlayerRole? = null,
)

data class GroupDetail(
    val id: String,
    val name: String,
    val players: List<PlayerDetail>,
    val invitedUsers: List<String>,
)

data class PlayerDetail(
    val id: String,
    val nickName: String,
    val role: PlayerRole,
    val status: PlayerStatusType,
)

data class InviteUserCommand(
    val inviterId: UserId,
    val inviteeId: UserId,
    val groupId: GroupId,
)

data class CreateGroupCommand(
    val userId: UserId,
    val name: Name,
)

data class ChangeGroupNameCommand(
    val userId: UserId,
    val groupId: GroupId,
    val newName: Name,
)

data class InviteUserResponseCommand(
    val userId: UserId,
    val groupId: GroupId,
    val response: Boolean,
)

data class UpdatePlayerRoleCommand(
    val userId: UserId,
    val updatingUserId: UserId,
    val groupId: GroupId,
    val newRole: PlayerRole,
)

data class UpdatePlayerStatusCommand(
    val userId: UserId,
    val updatingUserId: UserId,
    val groupId: GroupId,
    val newStatus: PlayerStatusType
)

@Service
class GroupCommandPort(
    private val aggregateStore: AggregateStore
) {
    suspend fun createGroup(command: CreateGroupCommand): GroupAggregate {
        return GroupAggregate(generateId()).also {
            it.createGroup(command)
            aggregateStore.save(it)
        }
    }

    suspend fun changeGroupName(command: ChangeGroupNameCommand) {
        aggregateStore.load(command.groupId.value, GroupAggregate::class.java).also {
            it.changeGroupName(command)
            aggregateStore.save(it)
        }
    }

    suspend fun inviteUser(command: InviteUserCommand) {
        aggregateStore.load(command.groupId.value, GroupAggregate::class.java).also {
            it.inviteUser(command)
            aggregateStore.save(it)
        }
    }

    suspend fun inviteUserResponse(command: InviteUserResponseCommand) {
        aggregateStore.load(command.groupId.value, GroupAggregate::class.java).also {
            it.inviteUserResponse(command)
            aggregateStore.save(it)
        }
    }

    suspend fun updatePlayerStatus(command: UpdatePlayerStatusCommand) {
        aggregateStore.load(command.groupId.value, GroupAggregate::class.java).also {
            it.updatePlayerStatus(command)
            aggregateStore.save(it)
        }
    }

    suspend fun updatePlayerRole(command: UpdatePlayerRoleCommand) {
        aggregateStore.load(command.groupId.value, GroupAggregate::class.java).also {
            it.updatePlayerRole(command)
            aggregateStore.save(it)
        }
    }
}

@Service
class GroupQueryPort(
    private val aggregateStore: AggregateStore,
) {
    suspend fun getActivePlayers(groupId: GroupId): List<UserId> =
        fetchGroup(groupId).players.filter { it.status.type() == PlayerStatusType.ACTIVE }.map { it.id }

    suspend fun isActiveMember(
        groupId: GroupId,
        userId: UserId
    ): Boolean = groupPersistencePort.findById(groupId)?.isActivePlayer(userId) ?: false

    suspend fun areActiveMembers(
        groupId: GroupId,
        userIds: Set<UserId>
    ): Boolean {
        val group = groupPersistencePort.findById(groupId) ?: return false
        return userIds.all { group.isActivePlayer(it) }
    }

    suspend fun isActiveAdmin(
        groupId: GroupId,
        userId: UserId
    ): Boolean = groupPersistencePort.findById(groupId)?.isActiveAdmin(userId) ?: false

    suspend fun getGroupsByPlayer(userId: UserId): List<Group> = groupPersistencePort.findByPlayer(userId)

    suspend fun getGroupDetails(groupId: GroupId, userId: UserId): GroupDetail {
        val group = fetchGroup(groupId).apply {
            require(this.players.any { it.id == userId }) { throw UserNotAuthorizedException(userId) }
        }

        val users = runBlocking {
            userApi.findUsersByIds(group.players.map { it.id }).associateBy { it.id }
        }

        return group.toGroupDetails(users)
    }


    private suspend fun fetchGroup(groupId: GroupId): Group =
        groupPersistencePort.findById(groupId) ?: throw GroupNotFoundException(groupId)

}