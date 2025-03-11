package ru.nspk.performance.keyvaluestorage.cassandra;


import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.jet.datamodel.Tuple2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class CassandraKeyValue<K, V> {
    private final String tableName;
    private final CqlSession session;
    private final long ttl;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public void put(K key, V value, Consumer<? super V> afterPutFunction) throws JsonProcessingException {
        String query = "INSERT INTO " + tableName + " (key, value) VALUES (?, ?) USING TTL " + ttl;
        session.execute(query, key, value);
        afterPutFunction.accept(value);
    }

    public Tuple2<Boolean, V> updateWithCondition(K key, Function<V, V> updateFunction, Function<V, Boolean> conditionFunction) {
        String query = "SELECT value FROM " + tableName + " WHERE key = ?";
        ResultSet resultSet = session.execute(query, key);
        Row row = resultSet.one();
        V currentValue = row != null ? (V) row.getObject("value") : null;
        boolean isValidState = currentValue != null && conditionFunction.apply(currentValue);
        V newValue = null;
        if (isValidState) {
            newValue = updateFunction.apply(currentValue);
            String updateQuery = "UPDATE " + tableName + " SET value = ? WHERE key = ? IF value = ?";
            session.execute(updateQuery, newValue, key, currentValue);
        }
        return Tuple2.tuple2(isValidState, isValidState ? newValue : currentValue);
    }

    public V get(K key) {
        String query = "SELECT value FROM " + tableName + " WHERE key = ?";
        ResultSet resultSet = session.execute(query, key);
        Row row = resultSet.one();
        return row != null ? (V) row.getObject("value") : null;
    }

    public CompletableFuture<V> getAsync(K key) {
        return CompletableFuture.supplyAsync(() -> get(key), executorService);
    }

    public V updateWithLock(K key, Function<V, V> updateFunction) {
        synchronized (key) { //todo исключить внешний параметр для синхронизации
            V currentValue = get(key);
            if (currentValue != null) {
                V newValue = updateFunction.apply(currentValue);
                String updateQuery = "UPDATE " + tableName + " SET value = ? WHERE key = ?";
                session.execute(updateQuery, newValue, key);
                return newValue;
            }
        }
        return null;
    }
}
