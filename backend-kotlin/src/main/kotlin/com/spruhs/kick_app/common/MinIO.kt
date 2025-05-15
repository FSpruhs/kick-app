package com.spruhs.kick_app.common

import com.spruhs.kick_app.user.core.domain.UserImagePort
import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import java.io.InputStream

@ConfigurationProperties("minio")
data class MinIOProperties(
    val url: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String,
)

@Configuration
@EnableConfigurationProperties(MinIOProperties::class)
class MinIOConfig {

    @Bean
    fun minIOClient(properties: MinIOProperties): MinioClient {
        return MinioClient.builder()
            .endpoint(properties.url)
            .credentials(properties.accessKey, properties.secretKey)
            .build()
    }
}

@Service
class MinIOImageStorage(
    private val minioClient: MinioClient,
    private val properties: MinIOProperties,
): UserImagePort {
    override fun save(inputStream: InputStream, contentType: String): UserImageId {
        val newId = generateId()

        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(properties.bucket)
                .`object`(newId)
                .stream(inputStream, -1, 10485760)
                .contentType(contentType)
                .build()
        )
        return UserImageId(newId)
    }

}