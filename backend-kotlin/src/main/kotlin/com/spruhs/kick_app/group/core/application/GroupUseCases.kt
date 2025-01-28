package com.spruhs.kick_app.group.core.application

import com.spruhs.kick_app.common.EventPublisher
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.group.core.domain.*
import org.springframework.stereotype.Service

@Service
class GroupUseCases(
    val groupPersistencePort: GroupPersistencePort,
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
}

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
