package ru.nspk.performance.transactionshandler.keyvaluestorage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.jet.datamodel.Tuple2;
import lombok.RequiredArgsConstructor;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class HazelcastManager implements KeyValueStorage {

    private final Map<String, HazelcastKeyValue> keyValueMap;

    @Override
    public <K, V> void put(String map, K key, V value, Consumer<? super V> afterPutFunction) throws JsonProcessingException, UnsupportedEncodingException {
        ((HazelcastKeyValue<K, V>) keyValueMap.get(map)).put(key, value, afterPutFunction);
    }

    @Override
    public <K, V> Tuple2<Boolean, V> updateWithCondition(String map, K key, Function<V, V> updateFunction, Function<V, Boolean> conditionFunction) {
        return ((HazelcastKeyValue<K, V>) keyValueMap.get(map)).updateWithCondition(key, updateFunction, conditionFunction);
    }

    @Override
    public <K, V> CompletionStage<V> getAsync(String map, K key) {
        return ((HazelcastKeyValue<K, V>) keyValueMap.get(map)).getAsync(key);
    }

    @Override
    public <K, V> V get(String map, K key) throws ExecutionException, InterruptedException, TimeoutException {
        return ((HazelcastKeyValue<K, V>) keyValueMap.get(map)).get(key);
    }
}
