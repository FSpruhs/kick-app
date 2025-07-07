package com.spruhs.kick_app.common.es

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.spruhs.kick_app.common.types.generateId
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.r2dbc.core.awaitOne
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.LocalDateTime
import java.util.*


abstract class AggregateRoot(open val aggregateId: String, open val aggregateType: String) {
    val changes: MutableList<BaseEvent> = mutableListOf()
    var version: Int = 0

    protected abstract fun whenEvent(event: BaseEvent)

    fun apply(event: BaseEvent) {
        changes.add(event)
        whenEvent(event)
        version++
    }

    fun raiseEvent(event: BaseEvent) {
        whenEvent(event)
        version++
    }

    fun loadEvents(events: MutableList<BaseEvent>) {
        events.forEach { whenEvent(it) }
    }

    fun clearChanges() {
        changes.clear()
    }

    override fun toString(): String {
        return "AggregateRoot(aggregateId='$aggregateId', aggregateType='$aggregateType', changes=$changes, version=$version)"
    }

}

class Event(val type: String, var data: ByteArray, var aggregateId: String, var aggregateType: String) {
    var id: String? = null
    var version: Int = 0
    var metadata: ByteArray = byteArrayOf()
    var timeStamp: LocalDateTime = LocalDateTime.now()

    constructor(aggregate: AggregateRoot, eventType: String, data: ByteArray, metadata: ByteArray) : this(
        type = eventType,
        data = data,
        aggregateId = aggregate.aggregateId,
        aggregateType = aggregate.aggregateType
    ) {
        this.id = generateId()
        this.version = aggregate.version
        this.metadata = metadata
        this.timeStamp = LocalDateTime.now()
    }

    constructor(
        type: String,
        aggregateType: String,
        id: String,
        version: Int,
        aggregateId: String,
        data: ByteArray,
        metadata: ByteArray,
        timeStamp: LocalDateTime
    ) : this(type, data, aggregateId, aggregateType) {
        this.id = id
        this.version = version
        this.metadata = metadata
        this.timeStamp = timeStamp
    }

    override fun toString(): String {
        return "Event(type='$type', data=${data.contentToString()}, aggregateId='$aggregateId', aggregateType='$aggregateType', id=$id, version=$version, metadata=${metadata.contentToString()}, timeStamp=$timeStamp)"
    }
}

class Snapshot(
    var id: UUID,
    var aggregateId: String,
    var aggregateType: String,
    var data: ByteArray,
    var metaData: ByteArray = byteArrayOf(),
    var version: Int = 0,
    var timeStamp: LocalDateTime = LocalDateTime.now()
) {
    override fun toString(): String {
        return "Snapshot(id=$id, aggregateId='$aggregateId', aggregateType='$aggregateType', data=${data.contentToString()}, metaData=${metaData.contentToString()}, version=$version, timeStamp=$timeStamp)"
    }
}

object EventSourcingConstants {
    const val AGGREGATE_ID = "aggregate_id"
    const val SNAPSHOT_ID = "snapshot_id"
    const val AGGREGATE_TYPE = "aggregate_type"
    const val DATA = "data"
    const val METADATA = "metadata"
    const val VERSION = "version"
    const val TIMESTAMP = "timestamp"
    const val EVENT_TYPE = "event_type"
    const val EVENT_ID = "event_id"
}

interface AggregateStore {
    suspend fun saveEvents(events: List<Event>)
    suspend fun loadEvents(aggregateId: String, version: Int): MutableIterable<Event>
    suspend fun <T : AggregateRoot> save(aggregate: T)
    suspend fun <T : AggregateRoot> load(aggregateId: String, aggregateType: Class<T>): T
}

interface Serializer {
    fun serialize(event: Any, aggregate: AggregateRoot): Event

    fun deserialize(event: Event): BaseEvent

    fun aggregateTypeName(): String
}

@Repository
class AggregateStoreImpl(
    private val dbClient: DatabaseClient,
    private val operator: TransactionalOperator,
    private val eventPublisher: EventPublisher,
    private val serializerFactory: SerializerFactory
) : AggregateStore {

    override suspend fun saveEvents(events: List<Event>) {
        return events.forEach { saveEvent(it) }
    }

    override suspend fun loadEvents(aggregateId: String, version: Int): MutableIterable<Event> {
        return withContext(Dispatchers.IO) {
            dbClient.sql(LOAD_EVENTS_QUERY)
                .bind(EventSourcingConstants.AGGREGATE_ID, aggregateId)
                .bind(EventSourcingConstants.VERSION, version)
                .map { row, meta -> eventFromRow(row, meta) }
                .all()
                .toIterable()
        }
    }

    override suspend fun <T : AggregateRoot> save(aggregate: T) {
        val serializer = serializerFactory.getSerializer(aggregate::class.java.simpleName)
        val events = aggregate.changes.map { serializer.serialize(it, aggregate) }

        operator.executeAndAwait {
            if (aggregate.version > 1) handleConcurrency(aggregate.aggregateId)

            saveEvents(events)

            if (aggregate.version % SNAPSHOT_FREQUENCY == 0) saveSnapshot(aggregate)

            eventPublisher.publish(aggregate.changes)
            aggregate.clearChanges()
        }
    }

    override suspend fun <T : AggregateRoot> load(aggregateId: String, aggregateType: Class<T>): T {
        val serializer = serializerFactory.getSerializer(aggregateType.simpleName)
        val snapshot = loadSnapshot(aggregateId)

        val aggregate = getAggregateFromSnapshotClass(snapshot, aggregateId, aggregateType)

        loadEvents(aggregateId, aggregate.version)
            .map { serializer.deserialize(it) }
            .forEach { aggregate.raiseEvent(it) }

        if (aggregate.version == 0) throw AggregateNotFoundException(aggregateId, aggregateType.name)

        return aggregate
    }

    private suspend fun saveEvent(event: Event) {
        return dbClient.sql(SAVE_EVENT_QUERY)
            .bind(EventSourcingConstants.EVENT_ID, event.id ?: "")
            .bind(EventSourcingConstants.AGGREGATE_ID, event.aggregateId)
            .bind(EventSourcingConstants.AGGREGATE_TYPE, event.aggregateType)
            .bind(EventSourcingConstants.EVENT_TYPE, event.type)
            .bind(EventSourcingConstants.VERSION, event.version)
            .bind(EventSourcingConstants.DATA, event.data)
            .bind(EventSourcingConstants.METADATA, event.metadata)
            .bind(EventSourcingConstants.TIMESTAMP, event.timeStamp)
            .await()
    }

    private suspend fun <T : AggregateRoot> getAggregateFromSnapshotClass(
        snapshot: Snapshot?,
        aggregateId: String,
        aggregateType: Class<T>
    ): T {
        if (snapshot == null) {
            val defaultSnapshot =
                EventSourcingUtils.snapshotFromAggregate(aggregate = getAggregate(aggregateId, aggregateType))
            return EventSourcingUtils.getAggregateFromSnapshot(defaultSnapshot, aggregateType)
        }

        return EventSourcingUtils.getAggregateFromSnapshot(snapshot, aggregateType)
    }

    private fun <T : AggregateRoot> getAggregate(aggregateId: String, aggregateType: Class<T>): T {
        return aggregateType.getConstructor(String::class.java).newInstance(aggregateId)
    }

    private suspend fun loadSnapshot(aggregateId: String): Snapshot? {
        return try {
            dbClient.sql(LOAD_SNAPSHOT_QUERY)
                .bind(EventSourcingConstants.AGGREGATE_ID, aggregateId)
                .map { row, meta -> snapshotFromRow(row, meta) }
                .awaitOne()
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    private suspend fun handleConcurrency(aggregateId: String) {
        dbClient.sql(HANDLE_CONCURRENCY_QUERY)
            .bind(EventSourcingConstants.AGGREGATE_ID, aggregateId)
            .await()
    }

    private suspend fun <T : AggregateRoot> saveSnapshot(aggregate: T) {
        val snapshot = EventSourcingUtils.snapshotFromAggregate(aggregate)

        dbClient.sql(SAVE_SNAPSHOT_QUERY)
            .bind(EventSourcingConstants.SNAPSHOT_ID, snapshot.id)
            .bind(EventSourcingConstants.AGGREGATE_ID, aggregate.aggregateId)
            .bind(EventSourcingConstants.AGGREGATE_TYPE, aggregate.aggregateType)
            .bind(EventSourcingConstants.DATA, snapshot.data)
            .bind(EventSourcingConstants.METADATA, snapshot.metaData)
            .bind(EventSourcingConstants.VERSION, snapshot.version)
            .bind(EventSourcingConstants.TIMESTAMP, snapshot.timeStamp)
            .await()
    }

    companion object {
        private const val SNAPSHOT_FREQUENCY = 25
        private const val SAVE_SNAPSHOT_QUERY =
            """INSERT INTO kick_app.snapshots (snapshot_id, aggregate_id, aggregate_type, data, metadata, version, timestamp) VALUES (:snapshot_id, :aggregate_id, :aggregate_type, :data, :metadata, :version, :timestamp) ON CONFLICT (aggregate_id) DO UPDATE SET data = :data, version = :version, timestamp = :timestamp"""
        private const val HANDLE_CONCURRENCY_QUERY =
            "SELECT aggregate_id FROM kick_app.events WHERE aggregate_id = :aggregate_id ORDER BY version LIMIT 1 FOR UPDATE"
        private const val LOAD_SNAPSHOT_QUERY =
            """SELECT aggregate_id, aggregate_type, data, metadata, version, timestamp FROM kick_app.snapshots s WHERE s.aggregate_id = :aggregate_id"""
        private const val SAVE_EVENT_QUERY =
            """INSERT INTO kick_app.events (event_id, aggregate_id, aggregate_type, event_type, data, metadata, version, timestamp) values (:event_id, :aggregate_id, :aggregate_type, :event_type, :data, :metadata, :version, :timestamp)"""
        private const val LOAD_EVENTS_QUERY =
            """SELECT event_id ,aggregate_id, aggregate_type, event_type, data, metadata, version, timestamp FROM kick_app.events e WHERE e.aggregate_id = :aggregate_id AND e.version > :version ORDER BY e.version ASC"""

        private fun snapshotFromRow(row: Row, meta: RowMetadata) = Snapshot(
            id = UUID.randomUUID(),
            aggregateId = row.get(EventSourcingConstants.AGGREGATE_ID, String::class.java) ?: "",
            aggregateType = row.get(EventSourcingConstants.AGGREGATE_TYPE, String::class.java) ?: "",
            data = row.get(EventSourcingConstants.DATA, ByteArray::class.java) ?: byteArrayOf(),
            metaData = row.get(EventSourcingConstants.METADATA, ByteArray::class.java) ?: byteArrayOf(),
            version = row.get(EventSourcingConstants.VERSION, Int::class.java) ?: 0,
            timeStamp = row.get(EventSourcingConstants.TIMESTAMP, LocalDateTime::class.java) ?: LocalDateTime.now()
        )

        private fun eventFromRow(row: Row, meta: RowMetadata) = Event(
            type = row.get(EventSourcingConstants.EVENT_TYPE, String::class.java) ?: "",
            aggregateId = row.get(EventSourcingConstants.AGGREGATE_ID, String::class.java) ?: "",
            aggregateType = row.get(EventSourcingConstants.AGGREGATE_TYPE, String::class.java) ?: "",
            id = row.get(EventSourcingConstants.EVENT_ID, String::class.java) ?: "",
            version = row.get(EventSourcingConstants.VERSION, Int::class.java) ?: 0,
            data = row.get(EventSourcingConstants.DATA, ByteArray::class.java) ?: byteArrayOf(),
            metadata = row.get(EventSourcingConstants.METADATA, ByteArray::class.java) ?: byteArrayOf(),
            timeStamp = row.get(EventSourcingConstants.TIMESTAMP, LocalDateTime::class.java) ?: LocalDateTime.now()
        )
    }
}

object EventSourcingUtils {
    private val mapper: ObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(ParameterNamesModule())
        .registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )

    fun <T> readValue(src: ByteArray, valueType: Class<T>): T {
        return mapper.readValue(src, valueType)
    }

    fun <T : AggregateRoot> snapshotFromAggregate(aggregate: T): Snapshot {
        val dataBytes = mapper.writeValueAsBytes(aggregate)
        return Snapshot(
            id = UUID.randomUUID(),
            aggregateId = aggregate.aggregateId,
            aggregateType = aggregate.aggregateType,
            data = dataBytes,
            version = aggregate.version,
            timeStamp = LocalDateTime.now()
        )
    }

    fun writeValueAsBytes(value: Any): ByteArray {
        return mapper.writeValueAsBytes(value)
    }

    fun <T : AggregateRoot> getAggregateFromSnapshot(snapshot: Snapshot, valueType: Class<T>): T {
        try {
            return mapper.readValue(snapshot.data, valueType)
        } catch (e: Exception) {
            throw SerializationException(valueType.name, snapshot.data)
        }

    }
}

@Component
class SerializerFactory(
    private val serializer: List<Serializer>,
) {
    fun getSerializer(aggregateType: String): Serializer {
        return serializer.firstOrNull { it.aggregateTypeName() == aggregateType } ?: throw IllegalArgumentException("Unknown aggregate type: $aggregateType")
    }
}

abstract class BaseEvent(open val aggregateId: String, var metadata: ByteArray = byteArrayOf())

data class AggregateNotFoundException(val aggregateId: String, val aggregateType: String) :
    Exception("Aggregate with id $aggregateId and type $aggregateType not found")

data class SerializationException(val valueType: String, val data: ByteArray) :
    Exception("Data: ${String(data)}, valueType: $valueType")

data class UnknownEventTypeException(val event: Any) : Exception("Unknown event type: $event")