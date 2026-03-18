package com.spruhs.kick_app.common.configs

import com.spruhs.kick_app.common.helper.getLogger
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
    private val log = getLogger(this::class.java)

    @Bean
    fun minIOClient(properties: MinIOProperties): MinioClient {
        val client =
            MinioClient
                .builder()
                .endpoint(properties.url)
                .credentials(properties.accessKey, properties.secretKey)
                .build()

        val bucket = properties.bucket
        val exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
            log.info("Created MinIO bucket '{}' during startup.", bucket)
        }

        return client
    }
}
