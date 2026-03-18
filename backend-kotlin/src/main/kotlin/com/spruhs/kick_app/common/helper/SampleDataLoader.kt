package com.spruhs.kick_app.common.helper

import com.spruhs.kick_app.common.configs.MinIOProperties
import io.minio.BucketExistsArgs
import io.minio.ListObjectsArgs
import io.minio.MinioClient
import io.minio.RemoveObjectArgs
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component

@Component
@Profile("dev")
class SampleDataLoader(
    private val importers: List<SampleDataImporter>,
    private val databaseCleaner: List<DatabaseCleaner>,
    @Value("\${app.load-sample-data}") private val loadSampleData: Boolean,
) {
    private val log = getLogger(this::class.java)

    @EventListener(ApplicationReadyEvent::class)
    suspend fun importAll() {
        log.info("SampleDataLoader started with loadSampleData=$loadSampleData")
        if (!loadSampleData) return
        log.info("Cleaning databases...")
        databaseCleaner.forEach { it.clean() }

        log.info("Loading sample data...")
        importers.forEach { it.import() }
    }
}

fun interface SampleDataImporter {
    suspend fun import()
}

fun interface DatabaseCleaner {
    suspend fun clean()
}

@Component
@Profile("dev")
class MongoDBCleaner(
    private val mongoTemplate: MongoTemplate,
) : DatabaseCleaner {
    private val log = getLogger(this::class.java)

    override suspend fun clean() {
        log.info("Cleaning MongoDB collections...")
        val collectionNames = mongoTemplate.collectionNames
        collectionNames.forEach { collectionName ->
            if (!collectionName.startsWith("system.")) {
                log.info("Dropping collection: $collectionName")
                mongoTemplate.dropCollection(collectionName)
            }
        }
        log.info("MongoDB collections cleaned!")
    }
}

@Component
@Profile("dev")
class PostgreSQLCleaner(
    private val client: DatabaseClient,
) : DatabaseCleaner {
    private val log = getLogger(this::class.java)

    override suspend fun clean() {
        log.info("Cleaning PostgreSQL tables...")

        client.sql("TRUNCATE TABLE kick_app.events RESTART IDENTITY CASCADE;").then().block()
        client.sql("TRUNCATE TABLE kick_app.snapshots RESTART IDENTITY CASCADE;").then().block()

        log.info("PostgreSQL tables cleaned!")
    }
}

@Component
@Profile("dev")
class MinIOCleaner(
    private val minioClient: MinioClient,
    private val minIOProperties: MinIOProperties,
) : DatabaseCleaner {
    private val log = getLogger(this::class.java)

    override suspend fun clean() {
        val bucket = minIOProperties.bucket
        val bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())

        if (!bucketExists) {
            log.info("MinIO bucket '{}' does not exist yet - skipping cleanup.", bucket)
            return
        }

        log.info("Cleaning MinIO bucket '{}'...", bucket)
        val objects =
            minioClient.listObjects(
                ListObjectsArgs
                    .builder()
                    .bucket(bucket)
                    .recursive(true)
                    .build(),
            )
        objects.forEach { result ->
            val item = result.get()
            minioClient.removeObject(
                RemoveObjectArgs
                    .builder()
                    .bucket(bucket)
                    .`object`(item.objectName())
                    .build(),
            )
        }

        log.info("MinIO bucket '{}' cleaned!", bucket)
    }
}
