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
        Group(
            user = command.userId,
            name = command.name,
        ).apply {
            groupPersistencePort.save(this)
        }
    }

    fun inviteUser(command: InviteUserCommand) {
        groupPersistencePort.findById(command.groupId)?.let {
            it.inviteUser(command.inviterId, command.inviteeId)
            groupPersistencePort.save(it)
            eventPublisher.publishAll(it.domainEvents)
        } ?: throw GroupNotFoundException(command.groupId)
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

