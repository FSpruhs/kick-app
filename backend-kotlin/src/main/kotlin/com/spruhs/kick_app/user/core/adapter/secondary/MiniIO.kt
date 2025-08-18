package com.spruhs.kick_app.user.core.adapter.secondary

import com.spruhs.kick_app.common.configs.MinIOProperties
import com.spruhs.kick_app.common.types.UserImageId
import com.spruhs.kick_app.common.types.generateId
import com.spruhs.kick_app.user.core.domain.UserImagePort
import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class MinIOUserImageAdapter(
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