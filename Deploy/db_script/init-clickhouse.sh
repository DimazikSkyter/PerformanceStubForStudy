#!/bin/bash

CLICKHOUSE_CLIENT="clickhouse-client -n"

$CLICKHOUSE_CLIENT <<-EOSQL
    CREATE DATABASE IF NOT EXISTS theatre;

    CREATE TABLE IF NOT EXISTS theatre.message (
        fio String,
        age Int32,
        address String,
        price Float64,
        creation DateTime,
        secondsToPay Int32,
        uidToPay String,
        eventName String,
        eventType Enum8('LOW' = 0, 'MIDDLE' = 1, 'HIGH' = 2)
    ) ENGINE = MergeTree()
    ORDER BY creation;
EOSQL
