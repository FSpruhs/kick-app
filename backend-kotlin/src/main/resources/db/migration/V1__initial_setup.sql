CREATE TABLE IF NOT EXISTS kick_app.events
(
    event_id       VARCHAR(250) NOT NULL CHECK ( event_id <> '' ),
    aggregate_id   VARCHAR(250) NOT NULL CHECK ( aggregate_id <> '' ),
    aggregate_type VARCHAR(250) NOT NULL CHECK ( aggregate_type <> '' ),
    event_type     VARCHAR(250) NOT NULL CHECK ( event_type <> '' ),
    data           BYTEA,
    metadata       BYTEA,
    version        SERIAL       NOT NULL,
    timestamp      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (event_id, aggregate_id)
    ) PARTITION BY HASH (aggregate_id);

CREATE INDEX IF NOT EXISTS aggregate_id_aggregate_version_idx ON kick_app.events USING btree (aggregate_id, version ASC);

CREATE TABLE IF NOT EXISTS events_partition_hash_1 PARTITION OF kick_app.events
    FOR VALUES WITH (MODULUS 3, REMAINDER 0);

CREATE TABLE IF NOT EXISTS events_partition_hash_2 PARTITION OF kick_app.events
    FOR VALUES WITH (MODULUS 3, REMAINDER 1);

CREATE TABLE IF NOT EXISTS events_partition_hash_3 PARTITION OF kick_app.events
    FOR VALUES WITH (MODULUS 3, REMAINDER 2);

CREATE TABLE IF NOT EXISTS kick_app.snapshots
(
    snapshot_id    UUID PRIMARY KEY         ,
    aggregate_id   VARCHAR(250) UNIQUE NOT NULL CHECK ( aggregate_id <> '' ),
    aggregate_type VARCHAR(250)        NOT NULL CHECK ( aggregate_type <> '' ),
    data           BYTEA,
    metadata       BYTEA,
    version        SERIAL              NOT NULL,
    timestamp      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 UNIQUE (aggregate_id)
    );

CREATE INDEX IF NOT EXISTS aggregate_id_aggregate_version_idx ON kick_app.snapshots USING btree (aggregate_id, version);