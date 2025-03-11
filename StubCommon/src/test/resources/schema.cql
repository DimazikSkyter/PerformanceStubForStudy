CREATE KEYSPACE IF NOT EXISTS test_keyspace
    WITH replication = {'class':'SimpleStrategy', 'replication_factor':'1'};

CREATE TABLE IF NOT EXISTS test_keyspace.kv_store_str (
                                                      key TEXT PRIMARY KEY,
                                                      value TEXT
);


CREATE TABLE IF NOT EXISTS test_keyspace.kv_store_long (
                                                      key TEXT PRIMARY KEY,
                                                      value BIGINT
);