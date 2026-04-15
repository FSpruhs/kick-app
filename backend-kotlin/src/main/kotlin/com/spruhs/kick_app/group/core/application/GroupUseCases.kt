package com.spruhs.kick_app.group.core.application

import com.spruhs.kick_app.common.es.AggregateStore
import com.spruhs.kick_app.common.helper.KeyedMutex
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
    private val mutex: KeyedMutex<GroupId> = KeyedMutex(),
) {
    suspend fun createGroup(command: CreateGroupCommand): GroupAggregate =
        GroupAggregate(generateId()).also {
            it.createGroup(
                userId = command.userId,
                name = command.name,
            )
            aggregateStore.save(it)
        }

    suspend fun changeGroupName(command: ChangeGroupNameCommand) =
        handle(command.groupId) { group ->
            group.changeGroupName(
                userId = command.userId,
                newName = command.newName,
            )
        }

    suspend fun inviteUser(command: InviteUserCommand) {
        val userId = userApi.findUserIdByEmail(command.email) ?: throw IllegalStateException("User not found")
        handle(command.groupId) { group ->
            group.inviteUser(
                inviterId = command.inviterId,
                inviteeId = userId,
            )
        }
    }

    suspend fun inviteUserResponse(command: InviteUserResponseCommand) =
        handle(command.groupId) { group ->
            group.inviteUserResponse(
                userId = command.userId,
                response = command.response,
            )
        }

    suspend fun updatePlayerStatus(command: UpdatePlayerStatusCommand) =
        handle(command.groupId) { group ->
            group.updatePlayerStatus(
                userId = command.userId,
                updatingUserId = command.updatingUserId,
                newStatus = command.newStatus,
            )
        }

    suspend fun updatePlayerRole(command: UpdatePlayerRoleCommand) =
        handle(command.groupId) { group ->
            group.updatePlayerRole(
                userId = command.userId,
                updatingUserId = command.updatingUserId,
                newRole = command.newRole,
            )
        }

    private suspend fun loadGroup(groupId: GroupId): GroupAggregate = aggregateStore.load(groupId.value, GroupAggregate::class.java)

    private suspend inline fun handle(
        id: GroupId,
        crossinline block: (GroupAggregate) -> Unit,
    ) {
        mutex.withKeyLock(id) {
            loadGroup(id).also {
                block(it)
                aggregateStore.save(it)
            }
        }
    }
}
