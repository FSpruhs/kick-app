package com.spruhs.kick_app.group.core.application

import com.spruhs.kick_app.group.core.domain.Group
import com.spruhs.kick_app.group.core.domain.GroupPersistencePort
import com.spruhs.kick_app.group.core.domain.Name
import com.spruhs.kick_app.group.core.domain.UserId
import org.springframework.stereotype.Service

@Service
class GroupUseCases(val groupPersistencePort: GroupPersistencePort) {
    fun create(command: CreateGroupCommand) {
        Group(
            user = command.userId,
            name = command.name,
        ).apply {
            groupPersistencePort.save(this)
        }
    }
}

data class CreateGroupCommand(
    val userId: UserId,
    val name: Name,
)

