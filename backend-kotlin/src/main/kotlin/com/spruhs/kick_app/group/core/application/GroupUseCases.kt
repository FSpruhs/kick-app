package com.spruhs.kick_app.group.core.application

import com.spruhs.kick_app.common.es.AggregateStore
import com.spruhs.kick_app.common.types.Email
import com.spruhs.kick_app.common.types.GroupId
import com.spruhs.kick_app.common.types.PlayerRole
import com.spruhs.kick_app.common.types.PlayerStatusType
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.common.types.generateId
import com.spruhs.kick_app.group.core.domain.GroupAggregate
import com.spruhs.kick_app.group.core.domain.Name
import com.spruhs.kick_app.user.api.UserApi
import org.springframework.stereotype.Service

data class InviteUserCommand(
    val inviterId: UserId,
    val email: Email,
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
    val newStatus: PlayerStatusType,
)

@Service
class GroupCommandPort(
    private val aggregateStore: AggregateStore,
    private val userApi: UserApi,
) {
    suspend fun createGroup(command: CreateGroupCommand): GroupAggregate =
        GroupAggregate(generateId()).also {
            it.createGroup(
                userId = command.userId,
                name = command.name,
            )
            aggregateStore.save(it)
        }

    suspend fun changeGroupName(command: ChangeGroupNameCommand) {
        aggregateStore.load(command.groupId.value, GroupAggregate::class.java).also {
            it.changeGroupName(
                userId = command.userId,
                newName = command.newName,
            )
            aggregateStore.save(it)
        }
    }

    suspend fun inviteUser(command: InviteUserCommand) {
        val userId = userApi.findUserIdByEmail(command.email) ?: throw IllegalStateException("User not found")
        aggregateStore.load(command.groupId.value, GroupAggregate::class.java).also {
            it.inviteUser(
                inviterId = command.inviterId,
                inviteeId = userId,
            )
            aggregateStore.save(it)
        }
    }

    suspend fun inviteUserResponse(command: InviteUserResponseCommand) {
        aggregateStore.load(command.groupId.value, GroupAggregate::class.java).also {
            it.inviteUserResponse(
                userId = command.userId,
                response = command.response,
            )
            aggregateStore.save(it)
        }
    }

    suspend fun updatePlayerStatus(command: UpdatePlayerStatusCommand) {
        aggregateStore.load(command.groupId.value, GroupAggregate::class.java).also {
            it.updatePlayerStatus(
                userId = command.userId,
                updatingUserId = command.updatingUserId,
                newStatus = command.newStatus,
            )
            aggregateStore.save(it)
        }
    }

    suspend fun updatePlayerRole(command: UpdatePlayerRoleCommand) {
        aggregateStore.load(command.groupId.value, GroupAggregate::class.java).also {
            it.updatePlayerRole(
                userId = command.userId,
                updatingUserId = command.updatingUserId,
                newRole = command.newRole,
            )
            aggregateStore.save(it)
        }
    }
}
