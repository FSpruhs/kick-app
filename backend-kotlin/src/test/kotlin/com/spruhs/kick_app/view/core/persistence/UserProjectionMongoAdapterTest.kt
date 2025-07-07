package com.spruhs.kick_app.view.core.persistence

import com.spruhs.kick_app.AbstractMongoTest
import com.spruhs.kick_app.common.GroupId
import com.spruhs.kick_app.common.PlayerRole
import com.spruhs.kick_app.common.PlayerStatusType
import com.spruhs.kick_app.common.UserId
import com.spruhs.kick_app.user.TestUserBuilder
import com.spruhs.kick_app.view.core.service.UserGroupProjection
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class UserProjectionMongoAdapterTest : AbstractMongoTest() {

    @Autowired
    private lateinit var adapter: UserProjectionMongoDB

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `save should save user`(): Unit = runBlocking {
        // Given
        val userProjection1 = TestUserBuilder().buildProjection()
        val userProjection2 = TestUserBuilder().withId("not found").buildProjection()

        // When
        adapter.saveAll(listOf(userProjection1, userProjection2))

        // Then
        adapter.getUser(userProjection1.id).let { result ->
            assertThat(result).isNotNull()
            assertThat(result?.id).isEqualTo(userProjection1.id)
            assertThat(result?.nickName).isEqualTo(userProjection1.nickName)
            assertThat(result?.email).isEqualTo(userProjection1.email)
            assertThat(result?.userImageId).isEqualTo(userProjection1.userImageId)
            assertThat(result?.groups?.first()?.id).isEqualTo(userProjection1.groups.first().id)
            assertThat(result?.groups?.first()?.name).isEqualTo(userProjection1.groups.first().name)
            assertThat(result?.groups?.first()?.userRole).isEqualTo(userProjection1.groups.first().userRole)
            assertThat(result?.groups?.first()?.userStatus).isEqualTo(userProjection1.groups.first().userStatus)
            assertThat(result?.groups?.first()?.lastMatch?.toLocalDate()).isEqualTo(userProjection1.groups.first().lastMatch?.toLocalDate())
        }
    }

    @Test
    fun `getUser should return null for non-existing user`(): Unit = runBlocking {
        // Given
        val user = TestUserBuilder().buildProjection()

        adapter.save(user)

        // When
        adapter.getUser(UserId("not existing")).let { result ->
            // Then
            assertThat(result).isNull()
        }
    }

    @Test
    fun `findByGroupId should return group`(): Unit = runBlocking {
        // Given
        val group1 = UserGroupProjection(
            id = GroupId("groupId 1"),
            name = "Group 1",
            userStatus = PlayerStatusType.ACTIVE,
            userRole = PlayerRole.PLAYER,
        )

        val group2 = UserGroupProjection(
            id = GroupId("groupId 2"),
            name = "Group 2",
            userStatus = PlayerStatusType.ACTIVE,
            userRole = PlayerRole.PLAYER,
        )


        val user1 = TestUserBuilder()
            .withId("userId 1")
            .withGroups(listOf(group1))
            .buildProjection()
        val user2 = TestUserBuilder()
            .withId("userId 2")
            .withGroups(listOf(group1, group2))
            .buildProjection()
        val user3 = TestUserBuilder()
            .withId("userId 3")
            .withGroups(listOf(group2))
            .buildProjection()

        adapter.saveAll(listOf(user1, user2, user3))

        // When
        adapter.findByGroupId(group1.id).let { result ->
            // Then
            assertThat(result).hasSize(2)
            assertThat(result.map { it.id }).containsExactlyInAnyOrder(user1.id, user2.id)
        }
    }

    @Test
    fun `existsByEmail should return true for existing email`(): Unit = runBlocking {
        // Given
        val user1 = TestUserBuilder()
            .withId("userId 1")
            .withEmail("test mail 1")
            .buildProjection()
        val user2 = TestUserBuilder()
            .withId("userId 1")
            .withEmail("test mail 1")
            .buildProjection()
        adapter.saveAll(listOf(user1, user2))

        // When
        val exists = adapter.existsByEmail(user1.email)

        // Then
        assertThat(exists).isTrue()
    }

    @Test
    fun `existsByEmail should return false for non-existing email`(): Unit = runBlocking {
        // Given
        val user1 = TestUserBuilder()
            .withId("userId 1")
            .withEmail("test mail 1")
            .buildProjection()
        adapter.save(user1)

        // When
        val exists = adapter.existsByEmail("non-existing email")

        // Then
        assertThat(exists).isFalse()
    }

}