package com.spruhs.kick_app.common.configs

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
    @Bean
    fun minIOClient(properties: MinIOProperties): MinioClient =
        MinioClient
            .builder()
            .endpoint(properties.url)
            .credentials(properties.accessKey, properties.secretKey)
            .build()
}
