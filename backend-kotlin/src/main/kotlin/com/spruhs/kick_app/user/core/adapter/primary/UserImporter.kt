package com.spruhs.kick_app.user.core.adapter.primary

import com.spruhs.kick_app.common.es.AggregateStore
import com.spruhs.kick_app.common.helper.SampleDataImporter
import com.spruhs.kick_app.common.helper.getLogger
import com.spruhs.kick_app.common.types.Email
import com.spruhs.kick_app.common.types.UserId
import com.spruhs.kick_app.user.core.application.UserCommandsPort
import com.spruhs.kick_app.user.core.application.UserImageUpload
import com.spruhs.kick_app.user.core.domain.NickName
import com.spruhs.kick_app.user.core.domain.Password
import com.spruhs.kick_app.user.core.domain.UserAggregate
import com.spruhs.kick_app.user.core.domain.UserIdentityProviderPort
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component

@Component
@Profile("dev")
@Order(1)
class UserImporter(
    private val aggregateStore: AggregateStore,
    private val userIdentityProviderPort: UserIdentityProviderPort,
    private val userCommandsPort: UserCommandsPort,
) : SampleDataImporter {
    private val log = getLogger(this::class.java)
    private val resourcePatternResolver = PathMatchingResourcePatternResolver()

    companion object {
        private const val USER_ID_PREFIX = "user-id-"
        private const val SAMPLE_IMAGE_DIR = "sample-data/user-images"
    }

    override suspend fun import() {
        log.info("Starting to load sample user data...")
        defaultUsers.forEachIndexed { index, user -> createTestUser(user, index) }
        log.info("Sample user data loaded!")
    }

    private suspend fun createTestUser(
        data: Pair<NickName, Email>,
        index: Int,
    ) {
        val (nickName, email) = data
        val userId = UserId("$USER_ID_PREFIX${index + 1}")
        val user = UserAggregate(userId.value)
        user.createUser(email, nickName)
        aggregateStore.save(user)
        uploadUserImage(userId)
        userIdentityProviderPort.save(email, nickName, Password.fromPlaintext("Password123"), userId)
    }

    private suspend fun uploadUserImage(userId: UserId) {
        val imageResource =
            resourcePatternResolver
                .getResources("classpath:$SAMPLE_IMAGE_DIR/${userId.value}.*")
                .firstOrNull { it.exists() && it.isReadable }

        requireNotNull(imageResource) {
            "No sample user image found for ${userId.value} in classpath:$SAMPLE_IMAGE_DIR"
        }

        val fileName = requireNotNull(imageResource.filename) { "Sample image has no filename for ${userId.value}" }
        val contentType = requireNotNull(resolveContentType(fileName)) { "Unsupported sample image type for file: $fileName" }
        val bytes = imageResource.inputStream.use { it.readBytes() }

        userCommandsPort.updateUserImage(userId, UserImageUpload(bytes = bytes, contentType = contentType))
    }

    private fun resolveContentType(fileName: String): String? =
        when (fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "svg" -> "image/svg"
            else -> null
        }
}

private val defaultUsers =
    listOf(
        NickName("Spruhs") to Email("fabian@spruhs.com"),
        NickName("Andi") to Email("andreas@spruhs.com"),
        NickName("Casper") to Email("casper@kicken.com"),
        NickName("Jannick") to Email("jannick@kicken.com"),
        NickName("Enis") to Email("enis@kicken.com"),
        NickName("Deniz") to Email("deniz@kicken.com"),
        NickName("David") to Email("david@kicken.com"),
        NickName("Junis") to Email("junis@kicken.com"),
        NickName("Leon") to Email("leon@kicken.com"),
        NickName("Yüksel") to Email("yüksel@kicken.com"),
        NickName("Ahmet") to Email("ahmet@kicken.com"),
        NickName("Amon") to Email("amon@kicken.com"),
        NickName("Ben") to Email("ben@kicken.com"),
        NickName("Jan") to Email("jan@kicken.com"),
        NickName("Lukas") to Email("lukas@kicken.com"),
        NickName("Max") to Email("max@kicken.com"),
        NickName("Thorsten") to Email("thorsten@kicken.com"),
        NickName("Raul") to Email("raul@kicken.com"),
        NickName("phillip") to Email("phillip@kicken.com"),
        NickName("Frank") to Email("Frank@kicken.com"),
        NickName("Tönchen") to Email("Tönchen@kicken.com"),
        NickName("Lisa") to Email("Lisa@kicken.com"),
    )
