package com.spruhs.kick_app.common.helper

import com.spruhs.kick_app.common.configs.MinIOProperties
import com.spruhs.kick_app.common.types.UserImageId
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.http.Method
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Component

@Component
class MinioUrlService(
    private val minioClient: MinioClient,
    private val minIOProperties: MinIOProperties,
    environment: Environment,
) {
    private val log = getLogger(this::class.java)

    private val isDevProfileActive: Boolean = environment.acceptsProfiles(Profiles.of("dev"))

    fun toUrl(userImageId: UserImageId?): String? {
        if (userImageId == null) return null

        val url =
            minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs
                    .builder()
                    .method(Method.GET)
                    .bucket(minIOProperties.bucket)
                    .`object`(userImageId.value)
                    .expiry(60 * 30)
                    .build(),
            )

        if (url == null) {
            log.error("Failed to get presigned url for user image {}", userImageId.value)
            return null
        }

        return if (isDevProfileActive) url.replace("localhost", "10.0.2.2") else url
    }
}
