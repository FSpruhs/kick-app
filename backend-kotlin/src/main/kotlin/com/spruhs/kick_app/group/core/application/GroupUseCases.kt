package com.spruhs.kick_app.group.core.application

import com.spruhs.kick_app.common.EventPublisher
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.common.UserNotAuthorizedException
import com.spruhs.kick_app.group.core.domain.*
import com.spruhs.kick_app.user.api.UserApi
import org.springframework.stereotype.Service

@Service
class GroupUseCases(
    val groupPersistencePort: GroupPersistencePort,
    val userApi: UserApi,
    val eventPublisher: EventPublisher
) {
    fun create(command: CreateGroupCommand) {
        createGroup(
            user = command.userId,
            name = command.name,
        ).apply {
            groupPersistencePort.save(this)
        }
    }

    fun inviteUser(command: InviteUserCommand) {
        fetchGroup(command.groupId).inviteUser(command.inviterId, command.inviteeId).apply {
            groupPersistencePort.save(this)
            eventPublisher.publishAll(this.domainEvents)
        }
    }

    fun inviteUserResponse(command: InviteUserResponseCommand) {
        fetchGroup(command.groupId).inviteUserResponse(command.userId, command.response).apply {
            groupPersistencePort.save(this)
        }
    }

    private fun fetchGroup(groupId: GroupId): Group {
        return groupPersistencePort.findById(groupId) ?: throw GroupNotFoundException(groupId)
    }

    fun getGroupsByPlayer(userId: UserId): List<Group> {
        return groupPersistencePort.findByPlayer(userId)
    }

    fun leaveGroup(command: LeaveGroupCommand) {
        fetchGroup(command.groupId).leave(command.userId).apply {
            groupPersistencePort.save(this)
            eventPublisher.publishAll(this.domainEvents)
        }
    }

    fun removePlayer(command: RemovePlayerCommand) {
        fetchGroup(command.groupId).removePlayer(command.userId).apply {
            groupPersistencePort.save(this)
            eventPublisher.publishAll(this.domainEvents)
        }
    }

    fun getGroup(groupId: GroupId, userId: UserId): GroupDetail {
        val group = fetchGroup(groupId).apply {
            if (this.players.none { player -> player.id == userId }) {
                throw UserNotAuthorizedException(userId)
            }
        }

        val users = userApi.findUsersByIds(group.players.map { it.id }).associateBy { it.id }

        return GroupDetail(
            id = group.id.value,
            name = group.name.value,
            players = group.players.map { player ->
                PlayerDetail(
                    id = player.id.value,
                    nickName = users.getValue(player.id).nickName,
                    role = player.role,
                    status = player.status,
                )
            },
            invitedUsers = group.invitedUsers.map { it.value },
        )
    }

    fun updatePlayer(command: UpdatePlayerCommand) {
        fetchGroup(command.groupId).updatePlayer(
            command.requesterId,
            command.userId,
            command.newRole,
            command.newStatus
        ).apply {
            groupPersistencePort.save(this)
        }
    }
}

data class UpdatePlayerCommand(
    val requesterId: UserId,
    val userId: UserId,
    val groupId: GroupId,
    val newRole: PlayerRole,
    val newStatus: PlayerStatus
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
    val status: PlayerStatus,
)

data class LeaveGroupCommand(
    val userId: UserId,
    val groupId: GroupId,
)

data class RemovePlayerCommand(
    val requesterId: UserId,
    val userId: UserId,
    val groupId: GroupId,
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
