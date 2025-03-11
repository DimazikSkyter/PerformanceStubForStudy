package ru.nspk.performance.keyvaluestorage.cassandra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.jet.datamodel.Tuple2;
import lombok.RequiredArgsConstructor;
import ru.nspk.performance.keyvaluestorage.KeyValueStorage;
import ru.nspk.performance.keyvaluestorage.hazelcast.HazelcastKeyValue;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;


@RequiredArgsConstructor
public class CassandraManager implements KeyValueStorage {

    private final Map<String, CassandraKeyValue> keyValueMaps;

    @Override
    public <K, V> void put(String map, K key, V value, Consumer<? super V> afterPutFunction) throws JsonProcessingException, UnsupportedEncodingException {
        ((CassandraKeyValue<K, V>) keyValueMaps.get(map)).put(key, value, afterPutFunction);
    }

    @Override
    public <K> long getAndIncrement(String map, K key) throws InterruptedException {
        return Optional.ofNullable(((CassandraKeyValue<K, Long>) keyValueMaps.get(map)).updateWithLock(key, current -> {
            if (current == null) {
                return 1L;
            } else {
                return current + 1;
            }
        })).orElse(0L);
    }

    @Override
    public <K, V> Tuple2<Boolean, V> updateWithCondition(String map, K key, Function<V, V> updateFunction, Function<V, Boolean> conditionFunction) {
        return ((CassandraKeyValue<K, V>) keyValueMaps.get(map)).updateWithCondition(key, updateFunction, conditionFunction);
    }

    @Override
    public <K, V> CompletionStage<V> getAsync(String map, K key) {
        return ((CassandraKeyValue<K, V>) keyValueMaps.get(map)).getAsync(key);
    }

    @Override
    public <K, V> V get(String map, K key) throws ExecutionException, InterruptedException, TimeoutException {
        return ((CassandraKeyValue<K, V>) keyValueMaps.get(map)).get(key);
    }
}
