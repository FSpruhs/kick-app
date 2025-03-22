package com.spruhs.kick_app.group.core.application

import com.spruhs.kick_app.common.EventPublisher
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.group.core.domain.*
import com.spruhs.kick_app.user.api.UserApi
import com.spruhs.kick_app.user.api.UserData
import kotlinx.coroutines.*
import org.springframework.stereotype.Service

@Service
class GroupUseCases(
    private val groupPersistencePort: GroupPersistencePort,
    private val userApi: UserApi,
    private val eventPublisher: EventPublisher,
) {
    fun create(command: CreateGroupCommand) {
        createGroup(
            user = command.userId,
            name = command.name,
        ).apply {
            groupPersistencePort.save(this)
        }
    }

    fun getActivePlayers(groupId: GroupId): List<UserId> =
        fetchGroup(groupId).players.filter { it.status.type() == PlayerStatusType.ACTIVE }.map { it.id }

    fun isActiveMember(
        groupId: GroupId,
        userId: UserId
    ): Boolean = groupPersistencePort.findById(groupId)?.isActivePlayer(userId) ?: false

    fun areActiveMembers(
        groupId: GroupId,
        userIds: Set<UserId>
    ): Boolean {
        val group = groupPersistencePort.findById(groupId) ?: return false
        return userIds.all { group.isActivePlayer(it) }
    }

    fun isActiveAdmin(
        groupId: GroupId,
        userId: UserId
    ): Boolean = groupPersistencePort.findById(groupId)?.isActiveAdmin(userId) ?: false

    fun inviteUser(command: InviteUserCommand) {
        fetchGroup(command.groupId).inviteUser(command.inviterId, command.inviteeId).apply {
            groupPersistencePort.save(this)
            eventPublisher.publishAll(this.domainEvents)
        }
    }

    fun inviteUserResponse(command: InviteUserResponseCommand) {
        fetchGroup(command.groupId).inviteUserResponse(command.userId, command.response).apply {
            groupPersistencePort.save(this)
            eventPublisher.publishAll(this.domainEvents)
        }
    }

    fun updatePlayer(command: UpdatePlayerCommand) {
        fetchGroup(command.groupId)
            .run {
                command.newStatus?.let {
                    updatePlayerStatus(
                        userId = command.userId,
                        requestingUserId = command.updatingUserId,
                        newStatus = it
                    )
                } ?: this
            }
            .run {
                command.newRole?.let {
                    updatePlayerRole(
                        userId = command.userId,
                        requesterId = command.updatingUserId,
                        newRole = it
                    )
                } ?: this
            }.apply {
                groupPersistencePort.save(this)
                eventPublisher.publishAll(domainEvents)
            }
    }

    fun getGroupsByPlayer(userId: UserId): List<Group> = groupPersistencePort.findByPlayer(userId)

    fun getGroupDetails(groupId: GroupId, userId: UserId): GroupDetail {
        val group = fetchGroup(groupId).apply {
            require(this.players.any { it.id == userId }) { throw UserNotAuthorizedException(userId) }
        }

        val users = runBlocking {
            userApi.findUsersByIds(group.players.map { it.id }).associateBy { it.id }
        }

        return group.toGroupDetails(users)
    }


    private fun fetchGroup(groupId: GroupId): Group =
        groupPersistencePort.findById(groupId) ?: throw GroupNotFoundException(groupId)

}

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

data class InviteUserResponseCommand(
    val userId: UserId,
    val groupId: GroupId,
    val response: Boolean,
)
