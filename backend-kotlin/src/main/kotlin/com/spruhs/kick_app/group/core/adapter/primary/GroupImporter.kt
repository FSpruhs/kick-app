package com.spruhs.kick_app.group.core.adapter.primary

import com.spruhs.kick_app.common.getLogger
import com.spruhs.kick_app.group.core.adapter.secondary.GroupDocument
import com.spruhs.kick_app.group.core.adapter.secondary.GroupRepository
import com.spruhs.kick_app.group.core.adapter.secondary.PlayerDocument
import com.spruhs.kick_app.group.core.domain.PlayerRole
import com.spruhs.kick_app.group.core.domain.PlayerStatusType
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("dev")
class GroupImporter(private val groupRepository: GroupRepository) {

    @Value("\${app.load-default-data}")
    private var loadDefaultData: Boolean = false

    private val log = getLogger(this::class.java)

    @PostConstruct
    fun loadData() {
        if (!loadDefaultData) {
            return
        }

        groupRepository.deleteAll()
        groupRepository.saveAll(defaultGroups)

        log.info("Default group data loaded")

    }
}

private val defaultGroups: List<GroupDocument> = listOf(
    GroupDocument(
        id = "donnerstags-kick",
        name = "Donnerstags-kick",
        invitedUsers = listOf(),
        players = listOf(
            PlayerDocument(
                id = "da082e6e-b4c1-40a4-8144-9098a2d819d9",
                role = PlayerRole.ADMIN.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-2",
                role = PlayerRole.ADMIN.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-3",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-4",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-5",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-6",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-7",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.INACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-8",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-9",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-10",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.INACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-11",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-12",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-13",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-14",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-15",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-16",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-17",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-18",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            ),
            PlayerDocument(
                id = "user-id-19",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.LEAVED.toString(),
            ),
            PlayerDocument(
                id = "user-id-20",
                role = PlayerRole.PLAYER.toString(),
                status = PlayerStatusType.REMOVED.toString(),
            ),
        ),
    ),
    GroupDocument(
        id = "sonntags-kick",
        name = "Sonntags-kick",
        invitedUsers = listOf(),
        players = listOf(
            PlayerDocument(
                id = "da082e6e-b4c1-40a4-8144-9098a2d819d9",
                role = PlayerRole.ADMIN.toString(),
                status = PlayerStatusType.ACTIVE.toString(),
            )
        )
    )
)