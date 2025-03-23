package com.spruhs.kick_app.common

import java.time.LocalDateTime
import java.util.*


abstract class AggregateRoot(open val aggregateId: String, open val aggregateType: String) {
    val changes: MutableList<Any> = mutableListOf()
    var version: Int = 0

    protected abstract fun whenEvent(event: Any)

    fun apply(event: Any) {
        changes.add(event)
        whenEvent(event)
        version++
    }

    fun raiseEvent(event: Any) {
        whenEvent(event)
        version++
    }

    fun loadEvents(events: MutableList<Any>) {
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

interface AggregateStore {
    suspend fun saveEvents(events: List<Event>)
    suspend fun loadEvents(aggregateId: String, version: Int): List<Event>
    suspend fun <T: AggregateRoot> save(aggregate: T)
    suspend fun <T: AggregateRoot> load(aggregateId: String, aggregateType: Class<T>): T
}

