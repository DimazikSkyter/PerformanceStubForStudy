package ru.nspk.performance.keyvaluestorage.hazelcast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class HazelcastKeyValue<K, V> {

    private final String mapName;
    private final IMap<K, V> map;
    private final long timeToLive;
    private final long lockTimeoutMs;
    private final long timeout;

    private final ObjectMapper objectMapper = new ObjectMapper();


    public void put(K key, V value, Consumer<? super V> afterPutFunction) throws JsonProcessingException, UnsupportedEncodingException {

        map.putAsync(key,
                        value,
                        timeToLive,
                        TimeUnit.MILLISECONDS)
                .thenAccept(afterPutFunction);
    }

    public V updateWithLock(K key, Function<V, V> updateFunction) throws InterruptedException {
        boolean lock = map.tryLock(key, lockTimeoutMs, TimeUnit.MILLISECONDS);
        V value = null;
        try {
            if (lock) {
                value = map.get(key);
                V newValue = updateFunction.apply(value);
                map.put(key, newValue, timeToLive, TimeUnit.MILLISECONDS);
            }
        } finally {
            try {
                if (map.isLocked(key))
                    map.unlock(key);
            } catch (Exception e) {
                log.error("Failed to unlock map {} with key {}", mapName, key, e);
            }
        }
        return value;
    }

    public Tuple2<Boolean, V> updateWithCondition(K key,
                                                  Function<V, V> updateFunction,
                                                  Function<V, Boolean> conditionFunction) {
        V newValue = null;
        V currentValue = map.get(key);
        boolean isValidState = conditionFunction.apply(currentValue);
        if (isValidState) {
            newValue = updateFunction.apply(currentValue);
            map.put(key, newValue, timeToLive, TimeUnit.MILLISECONDS);
        }
        return Tuple2.tuple2(isValidState, isValidState ? newValue : currentValue);
    }

    public V get(K key) throws ExecutionException, InterruptedException, TimeoutException {
        log.debug("Get value for key {} in map", key, mapName);
        return map.getAsync(key)
                .toCompletableFuture()
                .get(timeout, TimeUnit.MILLISECONDS);
    }

    public CompletionStage<V> getAsync(K key) {
        return map.getAsync(key);
    }
}
